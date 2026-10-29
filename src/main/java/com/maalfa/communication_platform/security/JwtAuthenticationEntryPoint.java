package com.maalfa.communication_platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maalfa.communication_platform.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Fires whenever Spring Security rejects a request because there's no
 * valid Authentication in the SecurityContext — missing header, expired
 * token, bad signature, malformed token, etc. Without this, Spring's
 * default behavior is a bare 403 with no body, which is indistinguishable
 * from a genuine authorization failure (see UnauthorizedActionException).
 *
 * This only covers "not authenticated" (401). "Authenticated but not
 * permitted" still returns 403, via GlobalExceptionHandler for
 * UnauthorizedActionException thrown in controllers/services.
 *
 * Note: this fires from the security filter chain, before the
 * DispatcherServlet routes to a controller — so GlobalExceptionHandler's
 * @RestControllerAdvice can never catch this case, no matter what
 * exceptions it handles. That's why this exists as a separate class.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                          HttpServletResponse response,
                          AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Authentication token is missing, invalid, or expired")
                .path(request.getRequestURI())
                .build();

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
