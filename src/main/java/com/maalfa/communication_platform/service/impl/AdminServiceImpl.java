package com.maalfa.communication_platform.service.impl;

import com.maalfa.communication_platform.dto.user.UserResponse;
import com.maalfa.communication_platform.entity.Role;
import com.maalfa.communication_platform.entity.User;
import com.maalfa.communication_platform.exception.ResourceNotFoundException;
import com.maalfa.communication_platform.mapper.UserMapper;
import com.maalfa.communication_platform.repository.RefreshTokenRepository;
import com.maalfa.communication_platform.repository.RoleRepository;
import com.maalfa.communication_platform.repository.UserRepository;
import com.maalfa.communication_platform.security.permission.PermissionConstants;
import com.maalfa.communication_platform.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;

    public AdminServiceImpl(UserRepository userRepository,
                            RoleRepository roleRepository,
                            RefreshTokenRepository refreshTokenRepository,
                            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userMapper = userMapper;
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionConstants.MANAGE_USERS + "')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionConstants.MANAGE_USERS + "')")
    @Transactional
    public void disableUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionConstants.MANAGE_USERS + "')")
    @Transactional
    public void enableUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionConstants.BAN_USER + "')")
    @Transactional
    public void banUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.setEnabled(false);
        user.setAccountNonLocked(false);
        userRepository.save(user);

        // Revoke every active session — a banned user shouldn't be able
        // to keep refreshing an already-issued access token.
        refreshTokenRepository.deleteAllByUser(user);
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionConstants.BAN_USER + "')")
    @Transactional
    public void unbanUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionConstants.MANAGE_USERS + "')")
    @Transactional
    public void assignRole(Long userId, String roleName) {
        User user = findUserOrThrow(userId);
        Role role = findRoleOrThrow(roleName);
        user.addRole(role);
        userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasAuthority('" + PermissionConstants.MANAGE_USERS + "')")
    @Transactional
    public void removeRole(Long userId, String roleName) {
        User user = findUserOrThrow(userId);
        Role role = findRoleOrThrow(roleName);
        user.removeRole(role);
        userRepository.save(user);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private Role findRoleOrThrow(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
    }
}