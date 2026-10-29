package com.maalfa.communication_platform.util;

import com.maalfa.communication_platform.entity.User;
import com.maalfa.communication_platform.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Static helper for reading "who is making this request" out of Spring
 * Security's context. Every service method that needs to check ownership
 * (e.g. "is this the message's sender?") or populate a caller-specific
 * field (e.g. GroupResponse.currentUserRole) should go through this
 * rather than each service re-implementing the SecurityContextHolder
 * lookup independently.
 *
 * Static by design — this reads thread-local security context, not any
 * injected state, so there's nothing to gain from making it a Spring bean.
 */
public final class SecurityUtil {

    private SecurityUtil() {
        // no instances
    }

    /**
     * Returns the currently authenticated User entity.
     * Throws IllegalStateException if called somewhere with no
     * authenticated user — that's a programming error (calling this from
     * a public endpoint), not a normal "not logged in" case, since Spring
     * Security's filter chain should have already rejected the request
     * with 401 before a controller/service method ever runs.
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new IllegalStateException(
                    "No authenticated user in security context — this method should only be called " +
                            "from behind an authenticated endpoint");
        }

        return userPrincipal.getUser();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Non-throwing variant — use this for code paths that legitimately
     * run both authenticated and unauthenticated (rare in this API, but
     * useful for e.g. a shared method that behaves slightly differently
     * for logged-in vs anonymous callers).
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserPrincipal;
    }
}
