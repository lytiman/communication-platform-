package com.maalfa.communication_platform.controller;

import com.maalfa.communication_platform.dto.chat.ChatListResponse;
import com.maalfa.communication_platform.dto.chat.PrivateMessageResponse;
import com.maalfa.communication_platform.dto.chat.SendPrivateMessageRequest;
import com.maalfa.communication_platform.service.PrivateChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class PrivateChatController {

    private final PrivateChatService privateChatService;

    public PrivateChatController(PrivateChatService privateChatService) {
        this.privateChatService = privateChatService;
    }

    @GetMapping
    public ResponseEntity<List<ChatListResponse>> getMyChats() {
        return ResponseEntity.ok(privateChatService.getMyChats());
    }

    @PostMapping("/messages")
    public ResponseEntity<PrivateMessageResponse> sendMessage(@Valid @RequestBody SendPrivateMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(privateChatService.sendMessage(request));
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<PrivateMessageResponse>> getChatMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(privateChatService.getChatMessages(chatId, page, size));
    }

    @PostMapping("/{chatId}/read")
    public ResponseEntity<Void> markChatAsRead(@PathVariable Long chatId) {
        privateChatService.markChatAsRead(chatId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        privateChatService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
}
