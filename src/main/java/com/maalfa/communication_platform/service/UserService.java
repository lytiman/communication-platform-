package com.maalfa.communication_platform.service;

import com.maalfa.communication_platform.dto.user.UpdateProfileRequest;
import com.maalfa.communication_platform.dto.user.UserResponse;
import com.maalfa.communication_platform.dto.user.UserSummaryDto;

import java.util.List;

public interface UserService {

    UserResponse getMyProfile();

    UserResponse getUserById(Long userId);

    // Partial update — only fields present (non-null) in the request are
    // changed, matching UpdateProfileRequest's PATCH-style contract.
    UserResponse updateProfile(UpdateProfileRequest request);

    // Used for "find a user to add to a group" / "start a new chat with"
    // flows — matches on username OR full name, case-insensitive.
    List<UserSummaryDto> searchUsers(String query);
}