package com.maalfa.communication_platform.service.impl;

import com.maalfa.communication_platform.dto.user.UpdateProfileRequest;
import com.maalfa.communication_platform.dto.user.UserResponse;
import com.maalfa.communication_platform.dto.user.UserSummaryDto;
import com.maalfa.communication_platform.entity.User;
import com.maalfa.communication_platform.exception.ResourceNotFoundException;
import com.maalfa.communication_platform.mapper.UserMapper;
import com.maalfa.communication_platform.repository.UserRepository;
import com.maalfa.communication_platform.service.UserService;
import com.maalfa.communication_platform.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserResponse getMyProfile() {
        User currentUser = SecurityUtil.getCurrentUser();
        return userMapper.toResponse(currentUser);
    }

    @Override
    public UserResponse getUserById(Long userId) {
        User user = findUserOrThrow(userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();

        // Only overwrite fields the client actually sent — this is a
        // partial update, not a full replace. A null fullName means
        // "leave it as-is," not "clear it."
        if (request.getFullName() != null) {
            currentUser.setFullName(request.getFullName());
        }
        if (request.getAvatarUrl() != null) {
            currentUser.setAvatarUrl(request.getAvatarUrl());
        }

        currentUser = userRepository.save(currentUser);
        return userMapper.toResponse(currentUser);
    }

    @Override
    public List<UserSummaryDto> searchUsers(String query) {
        return userRepository
                .findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(query, query)
                .stream()
                .map(userMapper::toSummaryDto)
                .toList();
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }
}