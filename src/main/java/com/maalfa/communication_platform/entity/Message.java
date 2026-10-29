package com.maalfa.communication_platform.entity;

import com.maalfa.communication_platform.enums.MessageStatus;
import com.maalfa.communication_platform.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A single message. Belongs to EXACTLY ONE of privateChat or channel —
 * never both, never neither. Enforced at the DB level with a CHECK
 * constraint (see schema/migration) and validated in the service layer
 * before persisting.
 */
@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // Exactly one of these two must be non-null.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "private_chat_id")
    private PrivateChat privateChat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    // For attachments (IMAGE/VIDEO/FILE/AUDIO types) — points to stored file URL.
    @Column(name = "attachment_url")
    private String attachmentUrl;

    // Self-referencing FK for replies/threads.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    @Column(name = "edited")
    @Builder.Default
    private boolean edited = false;

    @Column(name = "deleted")
    @Builder.Default
    private boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Validates the "exactly one context" invariant.
     * Call this in the service layer before persisting.
     */
    @Transient
    public boolean hasExactlyOneContext() {
        return (privateChat != null) ^ (channel != null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message = (Message) o;
        return id != null && id.equals(message.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
