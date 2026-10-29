package com.maalfa.communication_platform.mapper;

import com.maalfa.communication_platform.dto.user.UserResponse;
import com.maalfa.communication_platform.dto.user.UserSummaryDto;
import com.maalfa.communication_platform.entity.Role;
import com.maalfa.communication_platform.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserSummaryDto toSummaryDto(User user) {
        if (user == null) return null;
        return UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .onlineStatus(user.getOnlineStatus())
                .build();
    }

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .enabled(user.isEnabled())
                .onlineStatus(user.getOnlineStatus())
                .lastSeenAt(user.getLastSeenAt())
                .createdAt(user.getCreatedAt())
                .roleNames(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();
    }
}
