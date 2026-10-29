package com.maalfa.communication_platform.dto.group;

import com.maalfa.communication_platform.dto.channel.ChannelResponse;
import com.maalfa.communication_platform.dto.user.UserSummaryDto;
import com.maalfa.communication_platform.enums.GroupRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private String avatarUrl;
    private boolean isPrivate;
    private UserSummaryDto createdBy;
    private LocalDateTime createdAt;
    private int memberCount;

    // The requesting user's own role in this group — null if they're not
    // a member (e.g. viewing a public group's preview before joining).
    private GroupRole currentUserRole;

    // Only populated on the group detail endpoint, not on "list my groups".
    private List<ChannelResponse> channels;

    // Only populated on the "list members" endpoint.
    private List<MemberInfo> members;

    // Nested rather than a separate top-level file — there's no dedicated
    // GroupMemberResponse in this project's dto structure.
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberInfo {
        private Long id;
        private UserSummaryDto user;
        private GroupRole role;
        private LocalDateTime joinedAt;
    }
}
