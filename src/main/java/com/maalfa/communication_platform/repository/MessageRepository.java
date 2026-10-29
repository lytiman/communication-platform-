package com.maalfa.communication_platform.repository;

import com.maalfa.communication_platform.entity.Channel;
import com.maalfa.communication_platform.entity.Message;
import com.maalfa.communication_platform.entity.PrivateChat;
import com.maalfa.communication_platform.entity.User;
import com.maalfa.communication_platform.enums.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Paginated so message history doesn't load an entire conversation at once.
    Page<Message> findAllByChannelOrderByCreatedAtDesc(Channel channel, Pageable pageable);

    Page<Message> findAllByPrivateChatOrderByCreatedAtDesc(PrivateChat privateChat, Pageable pageable);

    // Used to build each row's preview snippet on the "my chats" list.
    Optional<Message> findFirstByPrivateChatOrderByCreatedAtDesc(PrivateChat privateChat);

    // Unread count for a chat = messages NOT sent by me that aren't
    // marked READ yet. Used to populate ChatListResponse.unreadCount.
    long countByPrivateChatAndSenderNotAndStatusNot(PrivateChat privateChat, User sender, MessageStatus status);

    // Bulk mark-as-read — called when the current user opens a chat.
    // Deliberately excludes the current user's own messages (you don't
    // mark your own sent messages "read").
    @Modifying
    @Query("UPDATE Message m SET m.status = :readStatus " +
            "WHERE m.privateChat = :privateChat AND m.sender <> :currentUser AND m.status <> :readStatus")
    int markChatMessagesAsRead(@Param("privateChat") PrivateChat privateChat,
                               @Param("currentUser") User currentUser,
                               @Param("readStatus") MessageStatus readStatus);
}
