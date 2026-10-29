package com.maalfa.communication_platform.service;

import com.maalfa.communication_platform.dto.auth.AuthResponse;
import com.maalfa.communication_platform.dto.auth.LoginRequest;
import com.maalfa.communication_platform.dto.auth.RegisterRequest;
import com.maalfa.communication_platform.entity.User;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshTokenValue);

    void logout(String refreshTokenValue);

    void logoutAllDevices(User user);
}
