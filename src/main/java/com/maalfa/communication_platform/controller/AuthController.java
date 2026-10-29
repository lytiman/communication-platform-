package com.maalfa.communication_platform.controller;

import com.maalfa.communication_platform.dto.auth.AuthResponse;
import com.maalfa.communication_platform.dto.auth.LoginRequest;
import com.maalfa.communication_platform.dto.auth.RefreshTokenRequest;
import com.maalfa.communication_platform.dto.auth.RegisterRequest;
import com.maalfa.communication_platform.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * All endpoints here are public (see SecurityConfig: "/api/auth/**"
 * is permitAll and JwtAuthenticationFilter.shouldNotFilter skips them) —
 * none of these require an existing valid token, since their entire
 * purpose is to produce one.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/hey")
    public String get(){
      return "Hello,World";
    }
    // Logs out ONE device (the one whose refresh token is provided).
    // "Log out everywhere" is a separate authenticated endpoint that will
    // live on UserController later, since it needs to know WHO is asking
    // rather than just which token to revoke.
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
