package com.maalfa.communication_platform.dto.channel;

import com.maalfa.communication_platform.dto.user.UserSummaryDto;
import com.maalfa.communication_platform.enums.ChannelRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelResponse {
    private Long id;
    private Long groupId;
    private String name;
    private String description;
    private boolean isPrivate;
    private LocalDateTime createdAt;
    private int memberCount;

    // The requesting user's own role in this channel — null if not a member.
    private ChannelRole currentUserRole;

    // Only populated when the requester has manage permission (channel
    // ADMIN or parent group OWNER/ADMIN) AND the channel is private —
    // this is effectively a secret, never shown to regular members.
    private String inviteCode;

    // Only populated on the "list members" endpoint.
    private List<MemberInfo> members;

    // Nested rather than a separate top-level file — there's no dedicated
    // ChannelMemberResponse in this project's dto structure.
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberInfo {
        private Long id;
        private UserSummaryDto user;
        private ChannelRole role;
        private LocalDateTime joinedAt;
    }
}