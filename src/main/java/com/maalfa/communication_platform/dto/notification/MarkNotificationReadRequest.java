package com.maalfa.communication_platform.dto.notification;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkNotificationReadRequest {

    // A list rather than a single id — lets the client mark several
    // notifications read at once (e.g. "mark all as read" sends every
    // currently-unread id in one request).
    @NotEmpty
    private List<Long> notificationIds;
}
