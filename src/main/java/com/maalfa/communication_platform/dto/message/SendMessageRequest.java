package com.maalfa.communication_platform.dto.message;

import com.maalfa.communication_platform.enums.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageRequest {

    @NotNull
    private Long channelId;

    private String content;

    @Builder.Default
    private MessageType type = MessageType.TEXT;

    private String attachmentUrl;

    private Long replyToId;
}
