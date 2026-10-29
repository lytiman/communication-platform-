package com.maalfa.communication_platform.service.impl;

import com.maalfa.communication_platform.dto.message.EditMessageRequest;
import com.maalfa.communication_platform.dto.message.MessageResponse;
import com.maalfa.communication_platform.dto.message.SendMessageRequest;
import com.maalfa.communication_platform.entity.Channel;
import com.maalfa.communication_platform.entity.Message;
import com.maalfa.communication_platform.entity.User;
import com.maalfa.communication_platform.enums.ChannelRole;
import com.maalfa.communication_platform.enums.GroupRole;
import com.maalfa.communication_platform.enums.MessageStatus;
import com.maalfa.communication_platform.exception.ResourceNotFoundException;
import com.maalfa.communication_platform.exception.UnauthorizedActionException;
import com.maalfa.communication_platform.mapper.MessageMapper;
import com.maalfa.communication_platform.repository.ChannelMemberRepository;
import com.maalfa.communication_platform.repository.ChannelRepository;
import com.maalfa.communication_platform.repository.GroupMemberRepository;
import com.maalfa.communication_platform.repository.MessageRepository;
import com.maalfa.communication_platform.service.MessageService;
import com.maalfa.communication_platform.util.SecurityUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageServiceImpl(MessageRepository messageRepository,
                              ChannelRepository channelRepository,
                              ChannelMemberRepository channelMemberRepository,
                              GroupMemberRepository groupMemberRepository,
                              MessageMapper messageMapper,
                              SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.channelMemberRepository = channelMemberRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.messageMapper = messageMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();
        Channel channel = findChannelOrThrow(request.getChannelId());
        requireChannelMembership(channel, currentUser);

        Message replyTo = null;
        if (request.getReplyToId() != null) {
            replyTo = messageRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Message", request.getReplyToId()));
            if (replyTo.getChannel() == null || !replyTo.getChannel().getId().equals(channel.getId())) {
                throw new IllegalArgumentException("Cannot reply to a message from a different channel");
            }
        }

        Message message = Message.builder()
                .sender(currentUser)
                .channel(channel)
                .content(request.getContent())
                .type(request.getType())
                .attachmentUrl(request.getAttachmentUrl())
                .replyTo(replyTo)
                .status(MessageStatus.SENT)
                .build();
        message = messageRepository.save(message);
        MessageResponse response = messageMapper.toResponse(message);

        // Push to everyone currently subscribed to this channel — see
        // WebSocketConfig for the destination convention.
        messagingTemplate.convertAndSend("/topic/channel." + channel.getId(), response);

        return response;
    }

    @Override
    public MessageResponse getMessageById(Long messageId) {
        Message message = findMessageOrThrow(messageId);
        User currentUser = SecurityUtil.getCurrentUser();
        requireChannelMembership(message.getChannel(), currentUser);

        return messageMapper.toResponse(message);
    }

    @Override
    public List<MessageResponse> getChannelMessages(Long channelId, int page, int size) {
        Channel channel = findChannelOrThrow(channelId);
        User currentUser = SecurityUtil.getCurrentUser();
        requireChannelMembership(channel, currentUser);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return messageRepository.findAllByChannelOrderByCreatedAtDesc(channel, pageable)
                .map(messageMapper::toResponse)
                .getContent();
    }

    @Override
    @Transactional
    public MessageResponse editMessage(Long messageId, EditMessageRequest request) {
        Message message = findMessageOrThrow(messageId);
        User currentUser = SecurityUtil.getCurrentUser();

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You can only edit your own messages");
        }
        if (message.isDeleted()) {
            throw new UnauthorizedActionException("Cannot edit a deleted message");
        }

        message.setContent(request.getContent());
        message.setEdited(true);
        message = messageRepository.save(message);
        MessageResponse response = messageMapper.toResponse(message);

        // Same channel topic as new messages — the client distinguishes
        // an edit from a new message via response.edited being true and
        // the id already existing in its local message list.
        messagingTemplate.convertAndSend("/topic/channel." + message.getChannel().getId(), response);

        return response;
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        Message message = findMessageOrThrow(messageId);
        User currentUser = SecurityUtil.getCurrentUser();

        boolean isSender = message.getSender().getId().equals(currentUser.getId());
        if (!isSender && !canModerateChannel(message.getChannel(), currentUser)) {
            throw new UnauthorizedActionException("You don't have permission to delete this message");
        }

        // Soft delete — row stays for audit purposes; MessageMapper blanks
        // content/attachmentUrl in the response once isDeleted() is true.
        message.setDeleted(true);
        message.setStatus(MessageStatus.DELETED);
        message = messageRepository.save(message);

        // Broadcast the now-blanked version so connected clients can
        // replace the message in place with "this message was deleted"
        // rather than needing a separate delete-event type.
        messagingTemplate.convertAndSend(
                "/topic/channel." + message.getChannel().getId(), messageMapper.toResponse(message));
    }

    // --- helpers ---

    private Channel findChannelOrThrow(Long channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> ResourceNotFoundException.of("Channel", channelId));
    }

    private Message findMessageOrThrow(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> ResourceNotFoundException.of("Message", messageId));
        if (message.getChannel() == null) {
            throw new IllegalStateException(
                    "Message " + messageId + " is a private message — use PrivateChatService instead");
        }
        return message;
    }

    private void requireChannelMembership(Channel channel, User user) {
        if (!channelMemberRepository.existsByChannelAndUser(channel, user)) {
            throw new UnauthorizedActionException("You must be a member of this channel to do that");
        }
    }

    // A channel ADMIN, or a group OWNER/ADMIN, can delete anyone's message
    // in that channel — the same "who can manage this channel" rule used
    // throughout ChannelService.
    private boolean canModerateChannel(Channel channel, User user) {
        boolean isChannelAdmin = channelMemberRepository.findByChannelAndUser(channel, user)
                .map(m -> m.getRole() == ChannelRole.ADMIN)
                .orElse(false);
        if (isChannelAdmin) {
            return true;
        }
        return groupMemberRepository.findByGroupAndUser(channel.getGroup(), user)
                .map(m -> m.getRole() == GroupRole.OWNER || m.getRole() == GroupRole.ADMIN)
                .orElse(false);
    }
}