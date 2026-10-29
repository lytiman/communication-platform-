package com.maalfa.communication_platform.dto.user;

import com.maalfa.communication_platform.enums.OnlineStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Full user detail — for "get my profile" / "get user by id" endpoints.
 * Deliberately excludes password. Includes role NAMES only (not full
 * Role/Privilege objects) since the client just needs to know what the
 * user can do, not the internal permission model.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private boolean enabled;
    private OnlineStatus onlineStatus;
    private LocalDateTime lastSeenAt;
    private LocalDateTime createdAt;
    private Set<String> roleNames;
}
