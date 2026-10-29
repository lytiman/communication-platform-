package com.maalfa.communication_platform.mapper;

import com.maalfa.communication_platform.dto.chat.ChatListResponse;
import com.maalfa.communication_platform.dto.chat.PrivateMessageResponse;
import com.maalfa.communication_platform.entity.Message;
import com.maalfa.communication_platform.entity.PrivateChat;
import com.maalfa.communication_platform.entity.User;
import org.springframework.stereotype.Component;

@Component
public class PrivateChatMapper {

    private final UserMapper userMapper;

    public PrivateChatMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public PrivateMessageResponse toMessageResponse(Message message) {
        if (message == null) return null;
        if (message.getPrivateChat() == null) {
            throw new IllegalStateException(
                    "Message " + message.getId() + " has no privateChat — use MessageMapper for channel messages");
        }

        return PrivateMessageResponse.builder()
                .id(message.getId())
                .sender(userMapper.toSummaryDto(message.getSender()))
                .privateChatId(message.getPrivateChat().getId())
                .content(message.isDeleted() ? null : message.getContent())
                .type(message.getType())
                .status(message.getStatus())
                .attachmentUrl(message.isDeleted() ? null : message.getAttachmentUrl())
                .replyToId(message.getReplyTo() != null ? message.getReplyTo().getId() : null)
                .edited(message.isEdited())
                .createdAt(message.getCreatedAt())
                .build();
    }

    // currentUser is required so we know which side is "the other person" --
    // the same PrivateChat looks different depending on who's asking.
    // lastMessagePreview/unreadCount are computed by the service layer
    // (e.g. truncate content, count unread via MessageRepository) and
    // passed in rather than derived here.
    public ChatListResponse toListResponse(PrivateChat chat, User currentUser,
                                            String lastMessagePreview, int unreadCount) {
        if (chat == null) return null;
        return ChatListResponse.builder()
                .chatId(chat.getId())
                .otherUser(userMapper.toSummaryDto(chat.getOtherUser(currentUser)))
                .lastMessagePreview(lastMessagePreview)
                .lastMessageAt(chat.getLastMessageAt())
                .unreadCount(unreadCount)
                .build();
    }
}
