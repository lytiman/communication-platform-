package com.maalfa.communication_platform.exception;

/**
 * Thrown for invalid uploads (wrong file type, too large, empty file) or
 * storage I/O failures. Mapped to HTTP 400 by GlobalExceptionHandler —
 * covers both "the client sent something bad" and "storage failed"
 * cases, since from the client's perspective both mean "try again."
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
