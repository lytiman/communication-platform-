package com.maalfa.communication_platform.controller;

import com.maalfa.communication_platform.dto.user.UpdateProfileRequest;
import com.maalfa.communication_platform.dto.user.UserResponse;
import com.maalfa.communication_platform.dto.user.UserSummaryDto;
import com.maalfa.communication_platform.service.AuthService;
import com.maalfa.communication_platform.service.UserService;
import com.maalfa.communication_platform.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSummaryDto>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(userService.searchUsers(query));
    }

    // "Log out everywhere" — revokes every refresh token for the current
    // user. Lives here (not AuthController) because it needs to know WHO
    // is asking, unlike the single-device logout in AuthController which
    // only needs a refresh token value.
    @PostMapping("/me/logout-all")
    public ResponseEntity<Void> logoutAllDevices() {
        authService.logoutAllDevices(SecurityUtil.getCurrentUser());
        return ResponseEntity.noContent().build();
    }
}
