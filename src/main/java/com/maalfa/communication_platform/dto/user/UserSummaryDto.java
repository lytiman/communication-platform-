package com.maalfa.communication_platform.dto.user;

import com.maalfa.communication_platform.enums.OnlineStatus;
import lombok.*;

/**
 * Minimal user info used wherever a User is nested inside another
 * response (message sender, group/channel member list, notification
 * actor). Never includes email, roles, or password.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDto {
    private Long id;
    private String username;
    private String fullName;
    private String avatarUrl;
    private OnlineStatus onlineStatus;
}