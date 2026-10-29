package com.maalfa.communication_platform.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank
    private String usernameOrEmail;

    @NotBlank
    private String password;

    // Optional label for the requesting device, e.g. "iPhone 15",
    // "Chrome on Windows" — stored on the RefreshToken for session
    // management (supports multi-device login).
    private String deviceInfo;
}
