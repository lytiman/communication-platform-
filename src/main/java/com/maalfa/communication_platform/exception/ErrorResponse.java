package com.maalfa.communication_platform.exception;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The single consistent JSON shape every error response takes,
 * regardless of which exception triggered it. Lives here rather than in
 * dto/ because it isn't a domain concept — it's infrastructure specific
 * to GlobalExceptionHandler.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;       // e.g. "Not Found", "Conflict"
    private String message;
    private String path;

    // Only populated for validation failures (@Valid on a request body) —
    // one entry per invalid field, e.g. "email: must be a well-formed email address".
    private List<String> validationErrors;
}
