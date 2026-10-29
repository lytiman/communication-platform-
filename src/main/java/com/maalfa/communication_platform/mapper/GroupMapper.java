package com.maalfa.communication_platform.mapper;

import com.maalfa.communication_platform.dto.channel.ChannelResponse;
import com.maalfa.communication_platform.dto.group.GroupResponse;
import com.maalfa.communication_platform.entity.Channel;
import com.maalfa.communication_platform.entity.ChannelMember;
import com.maalfa.communication_platform.entity.Group;
import com.maalfa.communication_platform.entity.GroupMember;
import com.maalfa.communication_platform.enums.ChannelRole;
import com.maalfa.communication_platform.enums.GroupRole;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupMapper {

    private final UserMapper userMapper;

    public GroupMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    // memberCount, currentUserRole, channels, and members are all passed in
    // explicitly rather than pulled from group.getMembers()/getChannels()
    // directly -- those are lazy collections, and touching them here (outside
    // a transaction) risks a LazyInitializationException. The service layer
    // should fetch these via the relevant repositories and pass them in.
    public GroupResponse toResponse(Group group, int memberCount, GroupRole currentUserRole,
                                    List<ChannelResponse> channels, List<GroupResponse.MemberInfo> members) {
        if (group == null) return null;
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .avatarUrl(group.getAvatarUrl())
                .isPrivate(group.isPrivate())
                .createdBy(userMapper.toSummaryDto(group.getCreatedBy()))
                .createdAt(group.getCreatedAt())
                .memberCount(memberCount)
                .currentUserRole(currentUserRole)
                .channels(channels)
                .members(members)
                .build();
    }

    public GroupResponse.MemberInfo toMemberInfo(GroupMember member) {
        if (member == null) return null;
        return GroupResponse.MemberInfo.builder()
                .id(member.getId())
                .user(userMapper.toSummaryDto(member.getUser()))
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    public ChannelResponse toChannelResponse(Channel channel, int memberCount,
                                             ChannelRole currentUserRole,
                                             List<ChannelResponse.MemberInfo> members,
                                             String inviteCode) {
        if (channel == null) return null;
        return ChannelResponse.builder()
                .id(channel.getId())
                .groupId(channel.getGroup().getId())
                .name(channel.getName())
                .description(channel.getDescription())
                .isPrivate(channel.isPrivate())
                .createdAt(channel.getCreatedAt())
                .memberCount(memberCount)
                .currentUserRole(currentUserRole)
                .members(members)
                .inviteCode(inviteCode)
                .build();
    }

    public ChannelResponse.MemberInfo toChannelMemberInfo(ChannelMember member) {
        if (member == null) return null;
        return ChannelResponse.MemberInfo.builder()
                .id(member.getId())
                .user(userMapper.toSummaryDto(member.getUser()))
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}