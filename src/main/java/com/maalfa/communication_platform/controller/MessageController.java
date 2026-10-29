package com.maalfa.communication_platform.controller;

import com.maalfa.communication_platform.dto.message.EditMessageRequest;
import com.maalfa.communication_platform.dto.message.MessageResponse;
import com.maalfa.communication_platform.dto.message.SendMessageRequest;
import com.maalfa.communication_platform.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.sendMessage(request));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<MessageResponse> getMessageById(@PathVariable Long messageId) {
        return ResponseEntity.ok(messageService.getMessageById(messageId));
    }

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<List<MessageResponse>> getChannelMessages(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(messageService.getChannelMessages(channelId, page, size));
    }

    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponse> editMessage(@PathVariable Long messageId,
                                                         @Valid @RequestBody EditMessageRequest request) {
        return ResponseEntity.ok(messageService.editMessage(messageId, request));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
}
