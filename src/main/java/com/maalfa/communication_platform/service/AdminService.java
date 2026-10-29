package com.maalfa.communication_platform.service;

import com.maalfa.communication_platform.dto.user.UserResponse;

import java.util.List;

public interface AdminService {

    // Requires MANAGE_USERS privilege (enforced via @PreAuthorize in the impl).
    List<UserResponse> getAllUsers();

    void disableUser(Long userId);

    void enableUser(Long userId);

    // Requires the more serious BAN_USER privilege. Bans also revoke every
    // active session (all refresh tokens) for the user — not just a flag
    // flip, since a banned user shouldn't keep using an already-issued
    // access token's refresh chain.
    void banUser(Long userId);

    void unbanUser(Long userId);

    void assignRole(Long userId, String roleName);

    void removeRole(Long userId, String roleName);
}