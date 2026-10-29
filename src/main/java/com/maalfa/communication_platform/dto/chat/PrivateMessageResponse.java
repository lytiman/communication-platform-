package com.maalfa.communication_platform.dto.chat;

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
public class PrivateMessageResponse {
    private Long id;
    private UserSummaryDto sender;
    private Long privateChatId;
    private String content;
    private MessageType type;
    private MessageStatus status;
    private String attachmentUrl;
    private Long replyToId;
    private boolean edited;
    private LocalDateTime createdAt;
}

