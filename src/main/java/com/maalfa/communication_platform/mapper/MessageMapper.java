package com.maalfa.communication_platform.mapper;

import com.maalfa.communication_platform.dto.message.MessageResponse;
import com.maalfa.communication_platform.entity.Message;
import org.springframework.stereotype.Component;

/**
 * Maps channel-scoped messages only. Private (1:1) messages go through
 * PrivateChatMapper.toMessageResponse() instead, since they use a
 * separate PrivateMessageResponse DTO.
 */
@Component
public class MessageMapper {

    private final UserMapper userMapper;

    public MessageMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public MessageResponse toResponse(Message message) {
        if (message == null) return null;
        if (message.getChannel() == null) {
            throw new IllegalStateException(
                    "Message " + message.getId() + " has no channel — use PrivateChatMapper for private messages");
        }

        return MessageResponse.builder()
                .id(message.getId())
                .sender(userMapper.toSummaryDto(message.getSender()))
                .channelId(message.getChannel().getId())
                .content(message.isDeleted() ? null : message.getContent())
                .type(message.getType())
                .status(message.getStatus())
                .attachmentUrl(message.isDeleted() ? null : message.getAttachmentUrl())
                .replyToId(message.getReplyTo() != null ? message.getReplyTo().getId() : null)
                .edited(message.isEdited())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
