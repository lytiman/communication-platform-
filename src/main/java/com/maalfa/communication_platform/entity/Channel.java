package com.maalfa.communication_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "channels", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    // e.g. TEXT vs VOICE channel — reuse MessageType if it fits, otherwise
    // introduce a dedicated ChannelType enum later if requirements grow.
    @Column(nullable = false)
    @Builder.Default
    private boolean isPrivate = false;

    // Only ever set when isPrivate is true. Null for public channels.
    // Regenerable via ChannelService.regenerateInviteCode() so a leaked
    // code can be invalidated without deleting/recreating the channel.
    @Column(name = "invite_code", unique = true, length = 20)
    private String inviteCode;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ChannelMember> members = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Channel)) return false;
        Channel channel = (Channel) o;
        return id != null && id.equals(channel.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}