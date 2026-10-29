package com.maalfa.communication_platform.service.impl;

import com.maalfa.communication_platform.dto.auth.AuthResponse;
import com.maalfa.communication_platform.dto.auth.LoginRequest;
import com.maalfa.communication_platform.dto.auth.RegisterRequest;
import com.maalfa.communication_platform.entity.RefreshToken;
import com.maalfa.communication_platform.entity.Role;
import com.maalfa.communication_platform.entity.User;
import com.maalfa.communication_platform.exception.DuplicateResourceException;
import com.maalfa.communication_platform.exception.InvalidCredentialsException;
import com.maalfa.communication_platform.exception.InvalidRefreshTokenException;
import com.maalfa.communication_platform.exception.ResourceNotFoundException;
import com.maalfa.communication_platform.mapper.UserMapper;
import com.maalfa.communication_platform.repository.RefreshTokenRepository;
import com.maalfa.communication_platform.repository.RoleRepository;
import com.maalfa.communication_platform.repository.UserRepository;
import com.maalfa.communication_platform.security.JwtService;
import com.maalfa.communication_platform.security.UserPrincipal;
import com.maalfa.communication_platform.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    // Matches the ROLE_USER name from security.permission.Role -- every
    // new registrant gets this by default. DataSeeder guarantees the row
    // exists before this service is ever called.
    private static final String DEFAULT_ROLE_NAME =
            com.maalfa.communication_platform.security.permission.Role.ROLE_USER.name();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Value("${app.jwt.refresh-token-expiration-ms:2592000000}") // 30 days default
    private long refreshTokenExpirationMs;

    public AuthServiceImpl(UserRepository userRepository,
                            RoleRepository roleRepository,
                            RefreshTokenRepository refreshTokenRepository,
                            PasswordEncoder passwordEncoder,
                            AuthenticationManager authenticationManager,
                            JwtService jwtService,
                            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE_NAME)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Default role " + DEFAULT_ROLE_NAME + " not found -- has DataSeeder run?"));

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .roles(roles)
                .build();

        user = userRepository.save(user);

        return buildAuthResponse(user, null);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            // Deliberately generic -- never reveals whether the username/email
            // itself existed, only that the combination was wrong.
            throw new InvalidCredentialsException();
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return buildAuthResponse(principal.getUser(), request.getDeviceInfo());
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not recognized"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException("Refresh token has expired -- please log in again");
        }

        User user = refreshToken.getUser();
        String deviceInfo = refreshToken.getDeviceInfo();

        // Rotate: delete the used refresh token and issue a brand new one.
        // Prevents a leaked/stolen refresh token from being replayed
        // indefinitely -- each refresh consumes the old token.
        refreshTokenRepository.delete(refreshToken);

        return buildAuthResponse(user, deviceInfo);
    }

    @Override
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.deleteByToken(refreshTokenValue);
    }

    @Override
    @Transactional
    public void logoutAllDevices(User user) {
        refreshTokenRepository.deleteAllByUser(user);
    }

    private AuthResponse buildAuthResponse(User user, String deviceInfo) {
        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .deviceInfo(deviceInfo)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();
        refreshToken = refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(userMapper.toResponse(user))
                .build();
    }
}
