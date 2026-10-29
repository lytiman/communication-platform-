package com.maalfa.communication_platform.dto.chat;

import com.maalfa.communication_platform.enums.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Keyed off recipientId rather than privateChatId — the client shouldn't
 * need to know whether a PrivateChat row already exists between the two
 * users. The service layer looks it up (or creates it) via
 * PrivateChatRepository.findByUsers(), then sends the message.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendPrivateMessageRequest {

    @NotNull
    private Long recipientId;

    private String content;

    @Builder.Default
    private MessageType type = MessageType.TEXT;

    private String attachmentUrl;

    private Long replyToId;
}
