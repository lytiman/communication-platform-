package com.maalfa.communication_platform.exception;

/**
 * Thrown when a requested entity (User, Group, Channel, Message, etc.)
 * doesn't exist. Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Convenience factory for the extremely common "X with id Y not found" case.
    public static ResourceNotFoundException of(String resourceName, Object id) {
        return new ResourceNotFoundException(resourceName + " not found with id: " + id);
    }
}
