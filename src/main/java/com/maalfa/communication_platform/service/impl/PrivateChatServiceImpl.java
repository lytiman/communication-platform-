package com.maalfa.communication_platform.service.impl;

import com.maalfa.communication_platform.dto.chat.ChatListResponse;
import com.maalfa.communication_platform.dto.chat.PrivateMessageResponse;
import com.maalfa.communication_platform.dto.chat.SendPrivateMessageRequest;
import com.maalfa.communication_platform.entity.Message;
import com.maalfa.communication_platform.entity.PrivateChat;
import com.maalfa.communication_platform.entity.User;
import com.maalfa.communication_platform.enums.MessageStatus;
import com.maalfa.communication_platform.enums.NotificationType;
import com.maalfa.communication_platform.exception.ResourceNotFoundException;
import com.maalfa.communication_platform.exception.UnauthorizedActionException;
import com.maalfa.communication_platform.mapper.PrivateChatMapper;
import com.maalfa.communication_platform.repository.MessageRepository;
import com.maalfa.communication_platform.repository.PrivateChatRepository;
import com.maalfa.communication_platform.repository.UserRepository;
import com.maalfa.communication_platform.service.NotificationService;
import com.maalfa.communication_platform.service.PrivateChatService;
import com.maalfa.communication_platform.util.SecurityUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrivateChatServiceImpl implements PrivateChatService {

    private static final int PREVIEW_MAX_LENGTH = 80;

    private final PrivateChatRepository privateChatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final PrivateChatMapper privateChatMapper;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public PrivateChatServiceImpl(PrivateChatRepository privateChatRepository,
                                  MessageRepository messageRepository,
                                  UserRepository userRepository,
                                  PrivateChatMapper privateChatMapper,
                                  NotificationService notificationService,
                                  SimpMessagingTemplate messagingTemplate) {
        this.privateChatRepository = privateChatRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.privateChatMapper = privateChatMapper;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public PrivateMessageResponse sendMessage(SendPrivateMessageRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();

        if (request.getRecipientId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Cannot send a private message to yourself");
        }

        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> ResourceNotFoundException.of("User", request.getRecipientId()));

        PrivateChat chat = privateChatRepository.findByUsers(currentUser, recipient)
                .orElseGet(() -> createChat(currentUser, recipient));

        Message replyTo = null;
        if (request.getReplyToId() != null) {
            replyTo = messageRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Message", request.getReplyToId()));
            if (replyTo.getPrivateChat() == null || !replyTo.getPrivateChat().getId().equals(chat.getId())) {
                throw new IllegalArgumentException("Cannot reply to a message from a different chat");
            }
        }

        Message message = Message.builder()
                .sender(currentUser)
                .privateChat(chat)
                .content(request.getContent())
                .type(request.getType())
                .attachmentUrl(request.getAttachmentUrl())
                .replyTo(replyTo)
                .status(MessageStatus.SENT)
                .build();
        message = messageRepository.save(message);

        chat.setLastMessageAt(message.getCreatedAt());
        privateChatRepository.save(chat);

        notificationService.notifyUser(
                recipient,
                currentUser,
                NotificationType.NEW_MESSAGE,
                currentUser.getUsername() + " sent you a message",
                "/chats/" + chat.getId()
        );

        PrivateMessageResponse response = privateChatMapper.toMessageResponse(message);

        // Push straight to the recipient's personal queue — Spring resolves
        // "/user/{username}/queue/messages" to whichever active session(s)
        // belong to that username (see WebSocketAuthInterceptor for how the
        // Principal gets attached).
        messagingTemplate.convertAndSendToUser(recipient.getUsername(), "/queue/messages", response);

        return response;
    }

    @Override
    public List<ChatListResponse> getMyChats() {
        User currentUser = SecurityUtil.getCurrentUser();

        return privateChatRepository.findAllForUser(currentUser).stream()
                .map(chat -> {
                    Message lastMessage = messageRepository
                            .findFirstByPrivateChatOrderByCreatedAtDesc(chat)
                            .orElse(null);
                    String preview = buildPreview(lastMessage);
                    int unreadCount = (int) messageRepository
                            .countByPrivateChatAndSenderNotAndStatusNot(chat, currentUser, MessageStatus.READ);

                    return privateChatMapper.toListResponse(chat, currentUser, preview, unreadCount);
                })
                .toList();
    }

    @Override
    public List<PrivateMessageResponse> getChatMessages(Long chatId, int page, int size) {
        PrivateChat chat = findChatOrThrow(chatId);
        User currentUser = SecurityUtil.getCurrentUser();
        requireParticipant(chat, currentUser);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return messageRepository.findAllByPrivateChatOrderByCreatedAtDesc(chat, pageable)
                .map(privateChatMapper::toMessageResponse)
                .getContent();
    }

    @Override
    @Transactional
    public void markChatAsRead(Long chatId) {
        PrivateChat chat = findChatOrThrow(chatId);
        User currentUser = SecurityUtil.getCurrentUser();
        requireParticipant(chat, currentUser);

        messageRepository.markChatMessagesAsRead(chat, currentUser, MessageStatus.READ);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> ResourceNotFoundException.of("Message", messageId));

        if (message.getPrivateChat() == null) {
            throw new IllegalStateException(
                    "Message " + messageId + " is a channel message — use MessageService instead");
        }

        User currentUser = SecurityUtil.getCurrentUser();
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You can only delete your own messages");
        }

        message.setDeleted(true);
        message.setStatus(MessageStatus.DELETED);
        messageRepository.save(message);
    }

    // --- helpers ---

    // Consistent ordering (lower id first) so PrivateChatRepository's
    // unique constraint on (userOne, userTwo) is meaningful — see the
    // comment on PrivateChat entity itself.
    private PrivateChat createChat(User currentUser, User recipient) {
        boolean currentIsFirst = currentUser.getId() < recipient.getId();
        PrivateChat chat = PrivateChat.builder()
                .userOne(currentIsFirst ? currentUser : recipient)
                .userTwo(currentIsFirst ? recipient : currentUser)
                .build();
        return privateChatRepository.save(chat);
    }

    private PrivateChat findChatOrThrow(Long chatId) {
        return privateChatRepository.findById(chatId)
                .orElseThrow(() -> ResourceNotFoundException.of("PrivateChat", chatId));
    }

    private void requireParticipant(PrivateChat chat, User user) {
        boolean isParticipant = chat.getUserOne().getId().equals(user.getId())
                || chat.getUserTwo().getId().equals(user.getId());
        if (!isParticipant) {
            throw new UnauthorizedActionException("You are not a participant of this chat");
        }
    }

    private String buildPreview(Message lastMessage) {
        if (lastMessage == null) {
            return null;
        }
        if (lastMessage.isDeleted()) {
            return "This message was deleted";
        }
        String content = lastMessage.getContent();
        if (content == null) {
            return null;
        }
        return content.length() > PREVIEW_MAX_LENGTH
                ? content.substring(0, PREVIEW_MAX_LENGTH) + "…"
                : content;
    }
}
