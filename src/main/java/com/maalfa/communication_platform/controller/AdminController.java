package com.maalfa.communication_platform.controller;

import com.maalfa.communication_platform.dto.user.UserResponse;
import com.maalfa.communication_platform.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Every endpoint here relies on AdminServiceImpl's own @PreAuthorize
 * checks (MANAGE_USERS / BAN_USER) rather than repeating permission
 * logic at the controller level — the controller stays a thin pass-through.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users/{userId}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable Long userId) {
        adminService.disableUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable Long userId) {
        adminService.enableUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<Void> banUser(@PathVariable Long userId) {
        adminService.banUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<Void> unbanUser(@PathVariable Long userId) {
        adminService.unbanUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/roles/{roleName}")
    public ResponseEntity<Void> assignRole(@PathVariable Long userId, @PathVariable String roleName) {
        adminService.assignRole(userId, roleName);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{userId}/roles/{roleName}")
    public ResponseEntity<Void> removeRole(@PathVariable Long userId, @PathVariable String roleName) {
        adminService.removeRole(userId, roleName);
        return ResponseEntity.noContent().build();
    }
}
