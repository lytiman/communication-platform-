package com.maalfa.communication_platform.websocket;

import com.maalfa.communication_platform.entity.User;
import com.maalfa.communication_platform.enums.OnlineStatus;
import com.maalfa.communication_platform.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Updates User.onlineStatus based on WebSocket connect/disconnect —
 * this is the piece that finally gives that field (unused since we
 * first built the User entity) real meaning. Also broadcasts the
 * change to /topic/presence so other connected clients can show
 * "online now" indicators without polling.
 *
 * Simplification worth knowing: this tracks CONNECTION presence, not
 * "viewing a specific chat" presence — so it doesn't (yet) suppress the
 * "every message sends a notification" behavior noted back in
 * PrivateChatServiceImpl. That would need per-chat presence tracking,
 * which is a reasonable future enhancement built on top of this.
 */
@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    @EventListener
    @Transactional
    public void handleSessionConnected(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal == null) {
            return; // unauthenticated session (see WebSocketAuthInterceptor) — nothing to track
        }
        updatePresence(principal.getName(), OnlineStatus.ONLINE);
    }

    @EventListener
    @Transactional
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal == null) {
            return;
        }
        updatePresence(principal.getName(), OnlineStatus.OFFLINE);
    }

    private void updatePresence(String username, OnlineStatus status) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setOnlineStatus(status);
            if (status == OnlineStatus.OFFLINE) {
                user.setLastSeenAt(LocalDateTime.now());
            }
            userRepository.save(user);

            messagingTemplate.convertAndSend("/topic/presence", Map.of(
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "status", status
            ));
        });
    }
}
