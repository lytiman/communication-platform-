package com.maalfa.communication_platform.exception;

/**
 * Thrown when an authenticated user is who they say they are, but isn't
 * allowed to do the specific thing they're attempting — e.g. editing
 * someone else's message, or a MEMBER trying to perform an ADMIN-only
 * action. Distinct from "not logged in" (that's a 401, handled by Spring
 * Security itself, not this class). Mapped to HTTP 403.
 */
public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}
