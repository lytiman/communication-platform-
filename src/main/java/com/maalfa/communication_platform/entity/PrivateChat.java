package com.maalfa.communication_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "private_chats", uniqueConstraints = {
        // Enforce a single chat row per unordered pair — service layer must
        // always store user_one_id < user_two_id to keep this constraint meaningful.
        @UniqueConstraint(columnNames = {"user_one_id", "user_two_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivateChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_one_id", nullable = false)
    private User userOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_two_id", nullable = false)
    private User userTwo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    // Convenience: returns the other participant given one side of the chat.
    public User getOtherUser(User self) {
        if (userOne.getId().equals(self.getId())) return userTwo;
        if (userTwo.getId().equals(self.getId())) return userOne;
        throw new IllegalArgumentException("User is not a participant of this chat");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrivateChat)) return false;
        PrivateChat that = (PrivateChat) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
