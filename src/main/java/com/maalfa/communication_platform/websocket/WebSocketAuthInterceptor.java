package com.maalfa.communication_platform.websocket;

import com.maalfa.communication_platform.security.CustomUserDetailsService;
import com.maalfa.communication_platform.security.JwtService;
import com.maalfa.communication_platform.security.UserPrincipal;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * WebSocket connections never go through JwtAuthenticationFilter (that's
 * an HTTP servlet filter; the STOMP handshake is a different pipeline).
 * This interceptor is the WebSocket equivalent — it runs specifically on
 * the STOMP CONNECT frame, reads the JWT the client sent as a STOMP
 * header, and attaches the authenticated Principal to the session so
 * every subsequent frame on this connection (SUBSCRIBE, etc.) knows who
 * the user is.
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public WebSocketAuthInterceptor(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String username = jwtService.extractUsername(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (userDetails instanceof UserPrincipal userPrincipal
                            && jwtService.isTokenValid(token, userPrincipal)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        accessor.setUser(authToken);
                    }
                } catch (Exception ex) {
                    // Invalid/expired token — leave the session without a
                    // Principal. Subscriptions to /user/** destinations will
                    // simply never receive anything for an unauthenticated
                    // session; nothing else in the app treats this as fatal.
                }
            }
        }

        return message;
    }
}
