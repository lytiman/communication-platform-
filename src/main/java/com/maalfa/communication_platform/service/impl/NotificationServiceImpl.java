package com.maalfa.communication_platform.service.impl;

import com.maalfa.communication_platform.dto.notification.MarkNotificationReadRequest;
import com.maalfa.communication_platform.dto.notification.NotificationResponse;
import com.maalfa.communication_platform.entity.Notification;
import com.maalfa.communication_platform.entity.User;
import com.maalfa.communication_platform.enums.NotificationType;
import com.maalfa.communication_platform.mapper.NotificationMapper;
import com.maalfa.communication_platform.repository.NotificationRepository;
import com.maalfa.communication_platform.service.NotificationService;
import com.maalfa.communication_platform.util.SecurityUtil;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   NotificationMapper notificationMapper,
                                   SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public List<NotificationResponse> getMyNotifications() {
        User currentUser = SecurityUtil.getCurrentUser();
        return notificationRepository.findAllByRecipientOrderByCreatedAtDesc(currentUser).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications() {
        User currentUser = SecurityUtil.getCurrentUser();
        return notificationRepository.findAllByRecipientAndReadFalseOrderByCreatedAtDesc(currentUser).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public long getUnreadCount() {
        User currentUser = SecurityUtil.getCurrentUser();
        return notificationRepository.countByRecipientAndReadFalse(currentUser);
    }

    @Override
    @Transactional
    public void markAsRead(MarkNotificationReadRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();

        List<Notification> notifications = notificationRepository.findAllById(request.getNotificationIds());

        // Filter to only notifications that actually belong to the current
        // user — silently drop the rest rather than erroring, since a
        // mismatched id here is almost always a stale client list, not
        // someone deliberately probing another user's notifications.
        List<Notification> owned = notifications.stream()
                .filter(n -> n.getRecipient().getId().equals(currentUser.getId()))
                .peek(n -> n.setRead(true))
                .toList();

        notificationRepository.saveAll(owned);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User currentUser = SecurityUtil.getCurrentUser();
        List<Notification> unread = notificationRepository
                .findAllByRecipientAndReadFalseOrderByCreatedAtDesc(currentUser);

        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public void notifyUser(User recipient, User actor, NotificationType type, String content, String referenceUrl) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .content(content)
                .referenceUrl(referenceUrl)
                .build();
        notification = notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(), "/queue/notifications", notificationMapper.toResponse(notification));
    }
}
