package com.maalfa.communication_platform.exception;

/**
 * Thrown specifically during login when the username/email or password
 * doesn't match. Kept deliberately generic in its message (never says
 * WHICH part was wrong) so the API doesn't leak whether a given
 * username/email exists in the system. Mapped to HTTP 401.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException() {
        super("Invalid username/email or password");
    }
}
