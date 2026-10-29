package com.maalfa.communication_platform.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Real-time is push-only in this app — clients still SEND messages via
 * the existing REST endpoints (POST /api/messages, POST /api/chats/messages),
 * since that's where validation/permission checks already live. WebSocket
 * exists purely so the server can PUSH updates to subscribed clients the
 * instant something happens, instead of clients polling REST endpoints.
 *
 * Destinations clients subscribe to:
 *   /topic/channel.{channelId}   — new messages in a channel
 *   /topic/presence              — online/offline status changes
 *   /user/queue/messages         — new private messages (Spring resolves
 *                                   "/user/..." to the specific authenticated
 *                                   session automatically)
 *   /user/queue/notifications    — new notifications
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // tighten to your real frontend origin(s) in production
                .withSockJS(); // fallback for browsers/proxies that block raw WebSocket
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
