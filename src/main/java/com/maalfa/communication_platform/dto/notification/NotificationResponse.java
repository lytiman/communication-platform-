package com.maalfa.communication_platform.dto.notification;

import com.maalfa.communication_platform.dto.user.UserSummaryDto;
import com.maalfa.communication_platform.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private UserSummaryDto actor;
    private NotificationType type;
    private String content;
    private String referenceUrl;
    private boolean read;
    private LocalDateTime createdAt;
}
