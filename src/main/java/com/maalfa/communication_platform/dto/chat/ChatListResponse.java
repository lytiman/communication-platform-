package com.maalfa.communication_platform.dto.chat;

import com.maalfa.communication_platform.dto.user.UserSummaryDto;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents ONE row in the "my conversations" list — the client renders
 * a List<ChatListResponse> as its chat list screen. Deliberately lighter
 * than PrivateMessageResponse's full history: just enough to render a
 * preview row (avatar, name, snippet, unread badge).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatListResponse {
    private Long chatId;

    // The OTHER participant relative to whoever is requesting this —
    // resolved server-side via PrivateChat.getOtherUser(currentUser).
    private UserSummaryDto otherUser;

    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
}

