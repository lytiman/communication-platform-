package com.maalfa.communication_platform.service;

import com.maalfa.communication_platform.dto.message.EditMessageRequest;
import com.maalfa.communication_platform.dto.message.MessageResponse;
import com.maalfa.communication_platform.dto.message.SendMessageRequest;

import java.util.List;

public interface MessageService {

    MessageResponse sendMessage(SendMessageRequest request);

    MessageResponse getMessageById(Long messageId);

    List<MessageResponse> getChannelMessages(Long channelId, int page, int size);

    // Sender only.
    MessageResponse editMessage(Long messageId, EditMessageRequest request);

    // Sender, OR a channel ADMIN, OR a group OWNER/ADMIN. Soft delete —
    // the row stays for audit purposes, content is just blanked in the
    // response (see MessageMapper's isDeleted() check).
    void deleteMessage(Long messageId);
}
