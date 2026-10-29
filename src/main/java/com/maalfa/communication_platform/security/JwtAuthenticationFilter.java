//package com.maalfa.communication_platform.security;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.lang.NonNull;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//import org.springframework.security.web.util.matcher.RequestMatcher;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.List;
//
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private static final String AUTH_HEADER = "Authorization";
//    private static final String BEARER_PREFIX = "Bearer ";
//
//    // Requests matching any of these never need a token — the filter
//    // skips straight through. Kept here (not just in SecurityConfig) so
//    // the filter doesn't waste effort trying to parse a token that was
//    // never going to be required in the first place.
//    private static final List<RequestMatcher> PUBLIC_PATHS = List.of(
//            new AntPathRequestMatcher("/api/auth/**"),
//            new AntPathRequestMatcher("/ws/**")
//    );
//
//    private final JwtService jwtService;
//    private final CustomUserDetailsService userDetailsService;
//
//    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
//        this.jwtService = jwtService;
//        this.userDetailsService = userDetailsService;
//    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        return PUBLIC_PATHS.stream().anyMatch(matcher -> matcher.matches(request));
//    }
//
//    @Override
//    protected void doFilterInternal(@NonNull HttpServletRequest request,
//                                     @NonNull HttpServletResponse response,
//                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
//
//        String authHeader = request.getHeader(AUTH_HEADER);
//
//        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String token = authHeader.substring(BEARER_PREFIX.length());
//
//        // Already authenticated on this request (e.g. an internal forward) — skip.
//        if (SecurityContextHolder.getContext().getAuthentication() != null) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        try {
//            String username = jwtService.extractUsername(token);
//            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//
//            if (userDetails instanceof UserPrincipal userPrincipal
//                    && jwtService.isTokenValid(token, userPrincipal)) {
//
//                UsernamePasswordAuthenticationToken authToken =
//                        new UsernamePasswordAuthenticationToken(
//                                userDetails, null, userDetails.getAuthorities());
//                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//            }
//        } catch (Exception ex) {
//            // Invalid/expired/malformed token — leave the context unauthenticated
//            // rather than throwing here. Spring Security's own entry point
//            // handles rejecting the request with 401 further down the chain.
//            SecurityContextHolder.clearContext();
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}

package com.maalfa.communication_platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // Requests matching any of these never need a token — the filter
    // skips straight through. Kept here (not just in SecurityConfig) so
    // the filter doesn't waste effort trying to parse a token that was
    // never going to be required in the first place.
    private static final List<RequestMatcher> PUBLIC_PATHS = List.of(
            new AntPathRequestMatcher("/api/auth/**"),
            new AntPathRequestMatcher("/ws/**")
    );

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PUBLIC_PATHS.stream().anyMatch(matcher -> matcher.matches(request));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // Already authenticated on this request (e.g. an internal forward) — skip.
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (userDetails instanceof UserPrincipal userPrincipal
                    && jwtService.isTokenValid(token, userPrincipal)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception ex) {
            // Invalid/expired/malformed token — leave the context unauthenticated
            // rather than throwing here. Spring Security's own entry point
            // handles rejecting the request with 401 further down the chain.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}