//package com.maalfa.communication_platform.service.impl;
//
//import com.maalfa.communication_platform.dto.channel.ChannelResponse;
//import com.maalfa.communication_platform.dto.channel.CreateChannelRequest;
//import com.maalfa.communication_platform.dto.channel.JoinChannelRequest;
//import com.maalfa.communication_platform.dto.channel.UpdateChannelRequest;
//import com.maalfa.communication_platform.entity.Channel;
//import com.maalfa.communication_platform.entity.ChannelMember;
//import com.maalfa.communication_platform.entity.Group;
//import com.maalfa.communication_platform.entity.GroupMember;
//import com.maalfa.communication_platform.entity.User;
//import com.maalfa.communication_platform.enums.ChannelRole;
//import com.maalfa.communication_platform.enums.GroupRole;
//import com.maalfa.communication_platform.exception.DuplicateResourceException;
//import com.maalfa.communication_platform.exception.ResourceNotFoundException;
//import com.maalfa.communication_platform.exception.UnauthorizedActionException;
//import com.maalfa.communication_platform.mapper.GroupMapper;
//import com.maalfa.communication_platform.repository.ChannelMemberRepository;
//import com.maalfa.communication_platform.repository.ChannelRepository;
//import com.maalfa.communication_platform.repository.GroupMemberRepository;
//import com.maalfa.communication_platform.repository.GroupRepository;
//import com.maalfa.communication_platform.repository.UserRepository;
//import com.maalfa.communication_platform.service.ChannelService;
//import com.maalfa.communication_platform.util.SecurityUtil;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//public class ChannelServiceImpl implements ChannelService {
//
//    private final ChannelRepository channelRepository;
//    private final ChannelMemberRepository channelMemberRepository;
//    private final GroupRepository groupRepository;
//    private final GroupMemberRepository groupMemberRepository;
//    private final UserRepository userRepository;
//    private final GroupMapper groupMapper;
//
//    public ChannelServiceImpl(ChannelRepository channelRepository,
//                              ChannelMemberRepository channelMemberRepository,
//                              GroupRepository groupRepository,
//                              GroupMemberRepository groupMemberRepository,
//                              UserRepository userRepository,
//                              GroupMapper groupMapper) {
//        this.channelRepository = channelRepository;
//        this.channelMemberRepository = channelMemberRepository;
//        this.groupRepository = groupRepository;
//        this.groupMemberRepository = groupMemberRepository;
//        this.userRepository = userRepository;
//        this.groupMapper = groupMapper;
//    }
//
//    @Override
//    @Transactional
//    public ChannelResponse createChannel(CreateChannelRequest request) {
//        User currentUser = SecurityUtil.getCurrentUser();
//        Group group = findGroupOrThrow(request.getGroupId());
//        requireGroupRole(group, currentUser, GroupRole.OWNER, GroupRole.ADMIN);
//
//        Channel channel = Channel.builder()
//                .group(group)
//                .name(request.getName())
//                .description(request.getDescription())
//                .isPrivate(request.isPrivate())
//                .inviteCode(request.isPrivate() ? generateInviteCode() : null)
//                .build();
//        channel = channelRepository.save(channel);
//
//        // Creator automatically becomes ADMIN of the channel they just made.
//        ChannelMember creatorMembership = ChannelMember.builder()
//                .channel(channel)
//                .user(currentUser)
//                .role(ChannelRole.ADMIN)
//                .build();
//        channelMemberRepository.save(creatorMembership);
//
//        return toDetailedResponse(channel, currentUser);
//    }
//
//    @Override
//    public ChannelResponse getChannelById(Long channelId) {
//        Channel channel = findChannelOrThrow(channelId);
//        User currentUser = SecurityUtil.getCurrentUser();
//        requireVisibility(channel, currentUser);
//
//        return toDetailedResponse(channel, currentUser);
//    }
//
//    @Override
//    public List<ChannelResponse> getChannelsByGroup(Long groupId) {
//        Group group = findGroupOrThrow(groupId);
//        User currentUser = SecurityUtil.getCurrentUser();
//
//        return channelRepository.findAllByGroup(group).stream()
//                .filter(channel -> isVisible(channel, currentUser))
//                .map(channel -> toDetailedResponse(channel, currentUser))
//                .toList();
//    }
//
//    @Override
//    public List<ChannelResponse.MemberInfo> getChannelMembers(Long channelId) {
//        Channel channel = findChannelOrThrow(channelId);
//        User currentUser = SecurityUtil.getCurrentUser();
//        requireChannelMembership(channel, currentUser);
//
//        return channelMemberRepository.findAllByChannel(channel).stream()
//                .map(groupMapper::toChannelMemberInfo)
//                .toList();
//    }
//
//    @Override
//    @Transactional
//    public ChannelResponse updateChannel(Long channelId, UpdateChannelRequest request) {
//        Channel channel = findChannelOrThrow(channelId);
//        User currentUser = SecurityUtil.getCurrentUser();
//        requireChannelManagePermission(channel, currentUser);
//
//        if (request.getName() != null) {
//            channel.setName(request.getName());
//        }
//        if (request.getDescription() != null) {
//            channel.setDescription(request.getDescription());
//        }
//        if (request.getIsPrivate() != null) {
//            boolean wasPrivate = channel.isPrivate();
//            channel.setPrivate(request.getIsPrivate());
//
//            // Toggling public -> private needs a fresh code; toggling
//            // private -> public should wipe any old code so it can't be
//            // reused if the channel is made private again later.
//            if (!wasPrivate && request.getIsPrivate()) {
//                channel.setInviteCode(generateInviteCode());
//            } else if (wasPrivate && !request.getIsPrivate()) {
//                channel.setInviteCode(null);
//            }
//        }
//        channel = channelRepository.save(channel);
//
//        return toDetailedResponse(channel, currentUser);
//    }
//
//    @Override
//    @Transactional
//    public void deleteChannel(Long channelId) {
//        Channel channel = findChannelOrThrow(channelId);
//        User currentUser = SecurityUtil.getCurrentUser();
//        // Deleting a channel is a group-level decision, not a channel-admin one.
//        requireGroupRole(channel.getGroup(), currentUser, GroupRole.OWNER, GroupRole.ADMIN);
//
//        channelRepository.delete(channel);
//    }
//
//    @Override
//    @Transactional
//    public ChannelResponse.MemberInfo joinChannel(JoinChannelRequest request) {
//        Channel channel = findChannelOrThrow(request.getChannelId());
//        User currentUser = SecurityUtil.getCurrentUser();
//
//        // Must already belong to the parent group before joining any of its channels.
//        if (!groupMemberRepository.existsByGroupAndUser(channel.getGroup(), currentUser)) {
//            throw new UnauthorizedActionException("You must be a member of this channel's group first");
//        }
//
//        if (channel.isPrivate()) {
//            String providedCode = request.getInviteCode();
//            if (providedCode == null || !providedCode.equals(channel.getInviteCode())) {
//                throw new UnauthorizedActionException(
//                        "This channel is private — a valid invite code is required to join");
//            }
//        }
//
//        if (channelMemberRepository.existsByChannelAndUser(channel, currentUser)) {
//            throw new DuplicateResourceException("You are already a member of this channel");
//        }
//
//        ChannelMember membership = ChannelMember.builder()
//                .channel(channel)
//                .user(currentUser)
//                .role(ChannelRole.MEMBER)
//                .build();
//        membership = channelMemberRepository.save(membership);
//
//        return groupMapper.toChannelMemberInfo(membership);
//    }
//
//    @Override
//    @Transactional
//    public void leaveChannel(Long channelId) {
//        Channel channel = findChannelOrThrow(channelId);
//        User currentUser = SecurityUtil.getCurrentUser();
//
//        ChannelMember membership = channelMemberRepository.findByChannelAndUser(channel, currentUser)
//                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this channel"));
//
//        channelMemberRepository.delete(membership);
//    }
//
//    @Override
//    @Transactional
//    public void removeMember(Long channelId, Long userId) {
//        Channel channel = findChannelOrThrow(channelId);
//        User currentUser = SecurityUtil.getCurrentUser();
//        requireChannelManagePermission(channel, currentUser);
//
//        User targetUser = userRepository.findById(userId)
//                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
//        ChannelMember membership = channelMemberRepository.findByChannelAndUser(channel, targetUser)
//                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this channel"));
//
//        channelMemberRepository.delete(membership);
//    }
//
//    @Override
//    @Transactional
//    public ChannelResponse regenerateInviteCode(Long channelId) {
//        Channel channel = findChannelOrThrow(channelId);
//        User currentUser = SecurityUtil.getCurrentUser();
//        requireChannelManagePermission(channel, currentUser);
//
//        if (!channel.isPrivate()) {
//            // Not mapped to a specific HTTP status yet — falls through to
//            // GlobalExceptionHandler's generic 500 handler for now. Worth
//            // adding a dedicated 400-level exception if this comes up often.
//            throw new IllegalStateException("Only private channels have an invite code to regenerate");
//        }
//
//        channel.setInviteCode(generateInviteCode());
//        channel = channelRepository.save(channel);
//
//        return toDetailedResponse(channel, currentUser);
//    }
//
//    // --- helpers ---
//
//    private Group findGroupOrThrow(Long groupId) {
//        return groupRepository.findById(groupId)
//                .orElseThrow(() -> ResourceNotFoundException.of("Group", groupId));
//    }
//
//    private Channel findChannelOrThrow(Long channelId) {
//        return channelRepository.findById(channelId)
//                .orElseThrow(() -> ResourceNotFoundException.of("Channel", channelId));
//    }
//
//    private void requireGroupRole(Group group, User user, GroupRole... allowedRoles) {
//        GroupMember membership = groupMemberRepository.findByGroupAndUser(group, user)
//                .orElseThrow(() -> new UnauthorizedActionException("You must be a member of this group to do that"));
//
//        for (GroupRole role : allowedRoles) {
//            if (membership.getRole() == role) {
//                return;
//            }
//        }
//        throw new UnauthorizedActionException("You don't have permission to perform this action");
//    }
//
//    private void requireChannelMembership(Channel channel, User user) {
//        if (!channelMemberRepository.existsByChannelAndUser(channel, user)) {
//            throw new UnauthorizedActionException("You must be a member of this channel to do that");
//        }
//    }
//
//    // Visible if: the channel isn't private, OR the user is already a
//    // member of the channel, OR the user is a group-level OWNER/ADMIN
//    // (admins can always see into channels they're responsible for).
//    private boolean isVisible(Channel channel, User user) {
//        if (!channel.isPrivate()) {
//            return true;
//        }
//        if (channelMemberRepository.existsByChannelAndUser(channel, user)) {
//            return true;
//        }
//        return groupMemberRepository.findByGroupAndUser(channel.getGroup(), user)
//                .map(m -> m.getRole() == GroupRole.OWNER || m.getRole() == GroupRole.ADMIN)
//                .orElse(false);
//    }
//
//    private void requireVisibility(Channel channel, User user) {
//        if (!isVisible(channel, user)) {
//            throw new UnauthorizedActionException("This channel is private — you don't have access");
//        }
//    }
//
//    // Allowed to update/manage channel settings & members if EITHER:
//    // - the user is ADMIN of this specific channel, OR
//    // - the user is OWNER/ADMIN of the parent group (group admins can
//    //   always manage any channel inside their group)
//    private boolean canManageChannel(Channel channel, User user) {
//        boolean isChannelAdmin = channelMemberRepository.findByChannelAndUser(channel, user)
//                .map(m -> m.getRole() == ChannelRole.ADMIN)
//                .orElse(false);
//        if (isChannelAdmin) {
//            return true;
//        }
//
//        return groupMemberRepository.findByGroupAndUser(channel.getGroup(), user)
//                .map(m -> m.getRole() == GroupRole.OWNER || m.getRole() == GroupRole.ADMIN)
//                .orElse(false);
//    }
//
//    private void requireChannelManagePermission(Channel channel, User user) {
//        if (!canManageChannel(channel, user)) {
//            throw new UnauthorizedActionException("You don't have permission to manage this channel");
//        }
//    }
//
//    private ChannelResponse toDetailedResponse(Channel channel, User currentUser) {
//        int memberCount = channelMemberRepository.findAllByChannel(channel).size();
//        ChannelRole currentUserRole = channelMemberRepository.findByChannelAndUser(channel, currentUser)
//                .map(ChannelMember::getRole)
//                .orElse(null);
//
//        // Invite code is only ever shown to someone who could actually use
//        // it to invite others — never to a regular member, and never on
//        // a non-private channel (there isn't one).
//        boolean canSeeInviteCode = channel.isPrivate() && canManageChannel(channel, currentUser);
//        String inviteCode = canSeeInviteCode ? channel.getInviteCode() : null;
//
//        return groupMapper.toChannelResponse(channel, memberCount, currentUserRole, null, inviteCode);
//    }
//
//    // Random 10-character alphanumeric code — short enough to share
//    // easily (e.g. in a chat message or link), long enough that guessing
//    // one isn't practical.
//    private String generateInviteCode() {
//        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
//    }
//}
package com.maalfa.communication_platform.service.impl;

import com.maalfa.communication_platform.dto.channel.ChannelResponse;
import com.maalfa.communication_platform.dto.channel.CreateChannelRequest;
import com.maalfa.communication_platform.dto.channel.JoinChannelRequest;
import com.maalfa.communication_platform.dto.channel.UpdateChannelRequest;
import com.maalfa.communication_platform.entity.Channel;
import com.maalfa.communication_platform.entity.ChannelMember;
import com.maalfa.communication_platform.entity.Group;
import com.maalfa.communication_platform.entity.GroupMember;
import com.maalfa.communication_platform.entity.User;
import com.maalfa.communication_platform.enums.ChannelRole;
import com.maalfa.communication_platform.enums.GroupRole;
import com.maalfa.communication_platform.exception.DuplicateResourceException;
import com.maalfa.communication_platform.exception.ResourceNotFoundException;
import com.maalfa.communication_platform.exception.UnauthorizedActionException;
import com.maalfa.communication_platform.mapper.GroupMapper;
import com.maalfa.communication_platform.repository.ChannelMemberRepository;
import com.maalfa.communication_platform.repository.ChannelRepository;
import com.maalfa.communication_platform.repository.GroupMemberRepository;
import com.maalfa.communication_platform.repository.GroupRepository;
import com.maalfa.communication_platform.repository.UserRepository;
import com.maalfa.communication_platform.service.ChannelService;
import com.maalfa.communication_platform.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupMapper groupMapper;

    public ChannelServiceImpl(ChannelRepository channelRepository,
                              ChannelMemberRepository channelMemberRepository,
                              GroupRepository groupRepository,
                              GroupMemberRepository groupMemberRepository,
                              UserRepository userRepository,
                              GroupMapper groupMapper) {
        this.channelRepository = channelRepository;
        this.channelMemberRepository = channelMemberRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.groupMapper = groupMapper;
    }

    @Override
    @Transactional
    public ChannelResponse createChannel(CreateChannelRequest request) {
        User currentUser = SecurityUtil.getCurrentUser();
        Group group = findGroupOrThrow(request.getGroupId());
        requireGroupRole(group, currentUser, GroupRole.OWNER, GroupRole.ADMIN);

        Channel channel = Channel.builder()
                .group(group)
                .name(request.getName())
                .description(request.getDescription())
                .isPrivate(request.isPrivate())
                .inviteCode(request.isPrivate() ? generateInviteCode() : null)
                .build();
        channel = channelRepository.save(channel);

        // Creator automatically becomes ADMIN of the channel they just made.
        ChannelMember creatorMembership = ChannelMember.builder()
                .channel(channel)
                .user(currentUser)
                .role(ChannelRole.ADMIN)
                .build();
        channelMemberRepository.save(creatorMembership);

        return toDetailedResponse(channel, currentUser);
    }

    @Override
    public ChannelResponse getChannelById(Long channelId) {
        Channel channel = findChannelOrThrow(channelId);
        User currentUser = SecurityUtil.getCurrentUser();
        requireVisibility(channel, currentUser);

        return toDetailedResponse(channel, currentUser);
    }

    @Override
    public List<ChannelResponse> getChannelsByGroup(Long groupId) {
        Group group = findGroupOrThrow(groupId);
        User currentUser = SecurityUtil.getCurrentUser();

        return channelRepository.findAllByGroup(group).stream()
                .filter(channel -> isVisible(channel, currentUser))
                .map(channel -> toDetailedResponse(channel, currentUser))
                .toList();
    }

    @Override
    public List<ChannelResponse.MemberInfo> getChannelMembers(Long channelId) {
        Channel channel = findChannelOrThrow(channelId);
        User currentUser = SecurityUtil.getCurrentUser();
        requireChannelMembership(channel, currentUser);

        return channelMemberRepository.findAllByChannel(channel).stream()
                .map(groupMapper::toChannelMemberInfo)
                .toList();
    }

    @Override
    @Transactional
    public ChannelResponse updateChannel(Long channelId, UpdateChannelRequest request) {
        Channel channel = findChannelOrThrow(channelId);
        User currentUser = SecurityUtil.getCurrentUser();
        requireChannelManagePermission(channel, currentUser);

        if (request.getName() != null) {
            channel.setName(request.getName());
        }
        if (request.getDescription() != null) {
            channel.setDescription(request.getDescription());
        }
        if (request.getIsPrivate() != null) {
            boolean wasPrivate = channel.isPrivate();
            channel.setPrivate(request.getIsPrivate());

            // Toggling public -> private needs a fresh code; toggling
            // private -> public should wipe any old code so it can't be
            // reused if the channel is made private again later.
            if (!wasPrivate && request.getIsPrivate()) {
                channel.setInviteCode(generateInviteCode());
            } else if (wasPrivate && !request.getIsPrivate()) {
                channel.setInviteCode(null);
            }
        }
        channel = channelRepository.save(channel);

        return toDetailedResponse(channel, currentUser);
    }

    @Override
    @Transactional
    public void deleteChannel(Long channelId) {
        Channel channel = findChannelOrThrow(channelId);
        User currentUser = SecurityUtil.getCurrentUser();
        // Deleting a channel is a group-level decision, not a channel-admin one.
        requireGroupRole(channel.getGroup(), currentUser, GroupRole.OWNER, GroupRole.ADMIN);

        channelRepository.delete(channel);
    }

    @Override
    @Transactional
    public ChannelResponse.MemberInfo joinChannel(JoinChannelRequest request) {
        Channel channel = findChannelOrThrow(request.getChannelId());
        User currentUser = SecurityUtil.getCurrentUser();

        // Must already belong to the parent group before joining any of its channels.
        if (!groupMemberRepository.existsByGroupAndUser(channel.getGroup(), currentUser)) {
            throw new UnauthorizedActionException("You must be a member of this channel's group first");
        }

        if (channel.isPrivate()) {
            String providedCode = request.getInviteCode();
            if (providedCode == null || !providedCode.equals(channel.getInviteCode())) {
                throw new UnauthorizedActionException(
                        "This channel is private — a valid invite code is required to join");
            }
        }

        if (channelMemberRepository.existsByChannelAndUser(channel, currentUser)) {
            throw new DuplicateResourceException("You are already a member of this channel");
        }

        ChannelMember membership = ChannelMember.builder()
                .channel(channel)
                .user(currentUser)
                .role(ChannelRole.MEMBER)
                .build();
        membership = channelMemberRepository.save(membership);

        return groupMapper.toChannelMemberInfo(membership);
    }

    @Override
    @Transactional
    public ChannelResponse.MemberInfo addMember(Long channelId, Long userId) {
        Channel channel = findChannelOrThrow(channelId);
        User currentUser = SecurityUtil.getCurrentUser();
        requireChannelManagePermission(channel, currentUser);

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        // Target must already belong to the parent group before being
        // added to one of its channels.
        if (!groupMemberRepository.existsByGroupAndUser(channel.getGroup(), targetUser)) {
            throw new UnauthorizedActionException("User must be a member of this channel's group first");
        }

        if (channelMemberRepository.existsByChannelAndUser(channel, targetUser)) {
            throw new DuplicateResourceException("User is already a member of this channel");
        }

        ChannelMember membership = ChannelMember.builder()
                .channel(channel)
                .user(targetUser)
                .role(ChannelRole.MEMBER)
                .build();
        membership = channelMemberRepository.save(membership);

        return groupMapper.toChannelMemberInfo(membership);
    }

    @Override
    @Transactional
    public void leaveChannel(Long channelId) {
        Channel channel = findChannelOrThrow(channelId);
        User currentUser = SecurityUtil.getCurrentUser();

        ChannelMember membership = channelMemberRepository.findByChannelAndUser(channel, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this channel"));

        channelMemberRepository.delete(membership);
    }

    @Override
    @Transactional
    public void removeMember(Long channelId, Long userId) {
        Channel channel = findChannelOrThrow(channelId);
        User currentUser = SecurityUtil.getCurrentUser();
        requireChannelManagePermission(channel, currentUser);

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        ChannelMember membership = channelMemberRepository.findByChannelAndUser(channel, targetUser)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this channel"));

        channelMemberRepository.delete(membership);
    }

    @Override
    @Transactional
    public ChannelResponse regenerateInviteCode(Long channelId) {
        Channel channel = findChannelOrThrow(channelId);
        User currentUser = SecurityUtil.getCurrentUser();
        requireChannelManagePermission(channel, currentUser);

        if (!channel.isPrivate()) {
            // Not mapped to a specific HTTP status yet — falls through to
            // GlobalExceptionHandler's generic 500 handler for now. Worth
            // adding a dedicated 400-level exception if this comes up often.
            throw new IllegalStateException("Only private channels have an invite code to regenerate");
        }

        channel.setInviteCode(generateInviteCode());
        channel = channelRepository.save(channel);

        return toDetailedResponse(channel, currentUser);
    }

    // --- helpers ---

    private Group findGroupOrThrow(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> ResourceNotFoundException.of("Group", groupId));
    }

    private Channel findChannelOrThrow(Long channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> ResourceNotFoundException.of("Channel", channelId));
    }

    private void requireGroupRole(Group group, User user, GroupRole... allowedRoles) {
        GroupMember membership = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new UnauthorizedActionException("You must be a member of this group to do that"));

        for (GroupRole role : allowedRoles) {
            if (membership.getRole() == role) {
                return;
            }
        }
        throw new UnauthorizedActionException("You don't have permission to perform this action");
    }

    private void requireChannelMembership(Channel channel, User user) {
        if (!channelMemberRepository.existsByChannelAndUser(channel, user)) {
            throw new UnauthorizedActionException("You must be a member of this channel to do that");
        }
    }

    // Visible if: the channel isn't private, OR the user is already a
    // member of the channel, OR the user is a group-level OWNER/ADMIN
    // (admins can always see into channels they're responsible for).
    private boolean isVisible(Channel channel, User user) {
        if (!channel.isPrivate()) {
            return true;
        }
        if (channelMemberRepository.existsByChannelAndUser(channel, user)) {
            return true;
        }
        return groupMemberRepository.findByGroupAndUser(channel.getGroup(), user)
                .map(m -> m.getRole() == GroupRole.OWNER || m.getRole() == GroupRole.ADMIN)
                .orElse(false);
    }

    private void requireVisibility(Channel channel, User user) {
        if (!isVisible(channel, user)) {
            throw new UnauthorizedActionException("This channel is private — you don't have access");
        }
    }

    // Allowed to update/manage channel settings & members if EITHER:
    // - the user is ADMIN of this specific channel, OR
    // - the user is OWNER/ADMIN of the parent group (group admins can
    //   always manage any channel inside their group)
    private boolean canManageChannel(Channel channel, User user) {
        boolean isChannelAdmin = channelMemberRepository.findByChannelAndUser(channel, user)
                .map(m -> m.getRole() == ChannelRole.ADMIN)
                .orElse(false);
        if (isChannelAdmin) {
            return true;
        }

        return groupMemberRepository.findByGroupAndUser(channel.getGroup(), user)
                .map(m -> m.getRole() == GroupRole.OWNER || m.getRole() == GroupRole.ADMIN)
                .orElse(false);
    }

    private void requireChannelManagePermission(Channel channel, User user) {
        if (!canManageChannel(channel, user)) {
            throw new UnauthorizedActionException("You don't have permission to manage this channel");
        }
    }

    private ChannelResponse toDetailedResponse(Channel channel, User currentUser) {
        int memberCount = channelMemberRepository.findAllByChannel(channel).size();
        ChannelRole currentUserRole = channelMemberRepository.findByChannelAndUser(channel, currentUser)
                .map(ChannelMember::getRole)
                .orElse(null);

        // Invite code is only ever shown to someone who could actually use
        // it to invite others — never to a regular member, and never on
        // a non-private channel (there isn't one).
        boolean canSeeInviteCode = channel.isPrivate() && canManageChannel(channel, currentUser);
        String inviteCode = canSeeInviteCode ? channel.getInviteCode() : null;

        return groupMapper.toChannelResponse(channel, memberCount, currentUserRole, null, inviteCode);
    }

    // Random 10-character alphanumeric code — short enough to share
    // easily (e.g. in a chat message or link), long enough that guessing
    // one isn't practical.
    private String generateInviteCode() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}