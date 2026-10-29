package com.maalfa.communication_platform.dto.message;

import com.maalfa.communication_platform.dto.user.UserSummaryDto;
import com.maalfa.communication_platform.enums.MessageStatus;
import com.maalfa.communication_platform.enums.MessageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private Long id;
    private UserSummaryDto sender;
    private Long channelId;
    private String content;
    private MessageType type;
    private MessageStatus status;
    private String attachmentUrl;
    private Long replyToId;
    private boolean edited;
    private LocalDateTime createdAt;
}
