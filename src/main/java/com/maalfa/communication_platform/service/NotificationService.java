package com.maalfa.communication_platform.service;

import com.maalfa.communication_platform.dto.notification.MarkNotificationReadRequest;
import com.maalfa.communication_platform.dto.notification.NotificationResponse;
import com.maalfa.communication_platform.entity.User;
import com.maalfa.communication_platform.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getMyNotifications();

    List<NotificationResponse> getUnreadNotifications();

    long getUnreadCount();

    // Only marks notifications that actually belong to the current user —
    // silently ignores any id in the request that doesn't (no error, since
    // that's most likely just a stale client-side list, not an attack worth
    // surfacing).
    void markAsRead(MarkNotificationReadRequest request);

    void markAllAsRead();

    // Internal-use method — called BY other services (GroupService,
    // ChannelService, MessageService, etc.) when something notification-worthy
    // happens, e.g. being added to a group or mentioned in a message.
    // Not exposed via any controller directly; "actor" may be null for
    // system-generated notifications (see Notification entity).
    void notifyUser(User recipient, User actor, NotificationType type, String content, String referenceUrl);
}