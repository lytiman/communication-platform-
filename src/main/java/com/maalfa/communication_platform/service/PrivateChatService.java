package com.maalfa.communication_platform.service;

import com.maalfa.communication_platform.dto.chat.ChatListResponse;
import com.maalfa.communication_platform.dto.chat.PrivateMessageResponse;
import com.maalfa.communication_platform.dto.chat.SendPrivateMessageRequest;

import java.util.List;

public interface PrivateChatService {

    // Auto-creates the PrivateChat row between the two users on their
    // very first message — the client never has to know whether a chat
    // already existed.
    PrivateMessageResponse sendMessage(SendPrivateMessageRequest request);

    List<ChatListResponse> getMyChats();

    List<PrivateMessageResponse> getChatMessages(Long chatId, int page, int size);

    // Marks every unread message FROM the other participant as read.
    // Called when the current user opens a chat.
    void markChatAsRead(Long chatId);

    // Sender only — there's no "chat admin" concept for a 1:1 conversation.
    void deleteMessage(Long messageId);
}
