package com.maalfa.communication_platform.mapper;

import com.maalfa.communication_platform.dto.notification.NotificationResponse;
import com.maalfa.communication_platform.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    private final UserMapper userMapper;

    public NotificationMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) return null;
        return NotificationResponse.builder()
                .id(notification.getId())
                .actor(userMapper.toSummaryDto(notification.getActor()))
                .type(notification.getType())
                .content(notification.getContent())
                .referenceUrl(notification.getReferenceUrl())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
