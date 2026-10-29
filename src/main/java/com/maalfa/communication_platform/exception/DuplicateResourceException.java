package com.maalfa.communication_platform.exception;

/**
 * Thrown when an operation would violate a uniqueness rule that's better
 * caught early with a clear message than left to surface as a raw DB
 * constraint violation — e.g. username/email already taken, user is
 * already a member of a group/channel. Mapped to HTTP 409.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
