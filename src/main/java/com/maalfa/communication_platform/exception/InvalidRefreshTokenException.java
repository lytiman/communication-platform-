package com.maalfa.communication_platform.exception;

/**
 * Thrown when a refresh token is missing, unknown, or expired
 * (see RefreshToken.isExpired()). Mapped to HTTP 401 — the client
 * should treat this the same as a login failure and re-authenticate.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
