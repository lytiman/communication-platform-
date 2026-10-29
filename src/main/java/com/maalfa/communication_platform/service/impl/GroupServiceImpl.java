package com.maalfa.communication_platform.service.impl;

import com.maalfa.communication_platform.dto.channel.ChannelResponse;
import com.maalfa.communication_platform.dto.group.AddGroupMemberRequest;
import com.maalfa.communication_platform.dto.group.CreateGroupRequest;
import com.maalfa.communication_platform.dto.group.GroupResponse;
import com.maalfa.communication_platform.entity.Channel;
import com.maalfa.communication_platform.entity.Group;
import com.maalfa.communication_platform.entity.GroupMember;
import com.maalfa.communication_platform.entity.User;
import com.maalfa.communication_platform.enums.GroupRole;
import com.maalfa.communication_platform.enums.NotificationType;
import com.maalfa.communication_platform.exception.DuplicateResourceException;
import com.maalfa.communication_platform.exception.ResourceNotFoundException;
import com.maalfa.communication_platform.exception.UnauthorizedActionException;
import com.maalfa.communication_platform.mapper.GroupMapper;
import com.maalfa.communication_platform.repository.ChannelRepository;
import com.maalfa.communication_platform.repository.GroupMemberRepository;
import com.maalfa.communication_platform.repository.GroupRepository;
import com.maalfa.communication_platform.repository.UserRepository;
import com.maalfa.communication_platform.service.GroupService;
import com.maalfa.communication_platform.service.NotificationService;
import com.maalfa.communication_platform.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final GroupMapper groupMapper;
    private final NotificationService notificationService;

    public GroupServiceImpl(GroupRepository groupRepository,
                            GroupMemberRepository groupMemberRepository,
                            ChannelRepository channelRepository,
                            UserRepository userRepository,
                            GroupMapper groupMapper,
                            NotificationService notificationService) {
        this.notificationService = notificationService;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
        this.groupMapper = groupMapper;
    }

    @Override
    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();

        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .avatarUrl(request.getAvatarUrl())
                .isPrivate(request.getIsPrivate() == null || request.getIsPrivate())
                .createdBy(currentUser)
                .build();
        group = groupRepository.save(group);

        // Creator automatically becomes OWNER of their own group.
        GroupMember ownerMembership = GroupMember.builder()
                .group(group)
                .user(currentUser)
                .role(GroupRole.OWNER)
                .build();
        groupMemberRepository.save(ownerMembership);

        return toDetailedResponse(group, currentUser);
    }

    @Override
    public GroupResponse getGroupById(Long groupId) {
        Group group = findGroupOrThrow(groupId);
        User currentUser = SecurityUtil.getCurrentUser();

        boolean isMember = groupMemberRepository.existsByGroupAndUser(group, currentUser);
        if (group.isPrivate() && !isMember) {
            throw new UnauthorizedActionException("This group is private — you must be a member to view it");
        }

        return toDetailedResponse(group, currentUser);
    }

    @Override
    public List<GroupResponse> getMyGroups() {
        User currentUser = SecurityUtil.getCurrentUser();
        return groupMemberRepository.findAllByUser(currentUser).stream()
                .map(membership -> toSummaryResponse(membership.getGroup(), membership.getRole()))
                .toList();
    }

    @Override
    public List<GroupResponse.MemberInfo> getGroupMembers(Long groupId) {
        Group group = findGroupOrThrow(groupId);
        User currentUser = SecurityUtil.getCurrentUser();
        requireMembership(group, currentUser);

        return groupMemberRepository.findAllByGroup(group).stream()
                .map(groupMapper::toMemberInfo)
                .toList();
    }

    @Override
    @Transactional
    public GroupResponse.MemberInfo addMember(Long groupId, AddGroupMemberRequest request) {
        Group group = findGroupOrThrow(groupId);
        User currentUser = SecurityUtil.getCurrentUser();
        requireRole(group, currentUser, GroupRole.OWNER, GroupRole.ADMIN);

        User userToAdd = userRepository.findById(request.getUserId())
                .orElseThrow(() -> ResourceNotFoundException.of("User", request.getUserId()));

        if (groupMemberRepository.existsByGroupAndUser(group, userToAdd)) {
            throw new DuplicateResourceException("User is already a member of this group");
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(userToAdd)
                .role(request.getRole())
                .build();
        member = groupMemberRepository.save(member);

        notificationService.notifyUser(
                userToAdd,
                currentUser,
                NotificationType.GROUP_INVITE,
                currentUser.getUsername() + " added you to " + group.getName(),
                "/groups/" + group.getId()
        );

        return groupMapper.toMemberInfo(member);
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long userId) {
        Group group = findGroupOrThrow(groupId);
        User currentUser = SecurityUtil.getCurrentUser();
        requireRole(group, currentUser, GroupRole.OWNER, GroupRole.ADMIN);

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        GroupMember membership = groupMemberRepository.findByGroupAndUser(group, targetUser)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this group"));

        if (membership.getRole() == GroupRole.OWNER) {
            throw new UnauthorizedActionException("Cannot remove the group owner");
        }

        groupMemberRepository.delete(membership);
    }

    @Override
    @Transactional
    public void leaveGroup(Long groupId) {
        Group group = findGroupOrThrow(groupId);
        User currentUser = SecurityUtil.getCurrentUser();

        GroupMember membership = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this group"));

        if (membership.getRole() == GroupRole.OWNER) {
            throw new UnauthorizedActionException(
                    "The owner cannot leave the group — transfer ownership or delete the group instead");
        }

        groupMemberRepository.delete(membership);
    }

    @Override
    @Transactional
    public void deleteGroup(Long groupId) {
        Group group = findGroupOrThrow(groupId);
        User currentUser = SecurityUtil.getCurrentUser();
        requireRole(group, currentUser, GroupRole.OWNER);

        // Cascades to GroupMember and Channel rows — see Group entity's
        // @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true).
        groupRepository.delete(group);
    }

    // --- helpers ---

    private Group findGroupOrThrow(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> ResourceNotFoundException.of("Group", groupId));
    }

    private void requireMembership(Group group, User user) {
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new UnauthorizedActionException("You must be a member of this group to do that");
        }
    }

    private void requireRole(Group group, User user, GroupRole... allowedRoles) {
        GroupMember membership = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new UnauthorizedActionException("You must be a member of this group to do that"));

        boolean allowed = false;
        for (GroupRole role : allowedRoles) {
            if (membership.getRole() == role) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new UnauthorizedActionException("You don't have permission to perform this action");
        }
    }

    private GroupResponse toDetailedResponse(Group group, User currentUser) {
        int memberCount = groupMemberRepository.findAllByGroup(group).size();
        GroupRole currentUserRole = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .map(GroupMember::getRole)
                .orElse(null);

        List<ChannelResponse> channels = channelRepository.findAllByGroup(group).stream()
                .map(this::toBasicChannelResponse)
                .toList();

        return groupMapper.toResponse(group, memberCount, currentUserRole, channels, null);
    }

    private GroupResponse toSummaryResponse(Group group, GroupRole currentUserRole) {
        int memberCount = groupMemberRepository.findAllByGroup(group).size();
        return groupMapper.toResponse(group, memberCount, currentUserRole, null, null);
    }

    // Lightweight channel info for embedding in a group's detail view —
    // full per-channel detail (members, current user's channel-specific
    // role) is ChannelService's job, not GroupService's.
    private ChannelResponse toBasicChannelResponse(Channel channel) {
        return groupMapper.toChannelResponse(channel, 0, null, null, null);
    }
}