//package com.maalfa.communication_platform.service;
//
//import com.maalfa.communication_platform.dto.channel.ChannelResponse;
//import com.maalfa.communication_platform.dto.channel.CreateChannelRequest;
//import com.maalfa.communication_platform.dto.channel.JoinChannelRequest;
//import com.maalfa.communication_platform.dto.channel.UpdateChannelRequest;
//
//import java.util.List;
//
//public interface ChannelService {
//
//    ChannelResponse createChannel(CreateChannelRequest request);
//
//    ChannelResponse getChannelById(Long channelId);
//
//    // All channels within a group that the current user is allowed to see
//    // (private channels are excluded unless the user is already a member).
//    List<ChannelResponse> getChannelsByGroup(Long groupId);
//
//    List<ChannelResponse.MemberInfo> getChannelMembers(Long channelId);
//
//    ChannelResponse updateChannel(Long channelId, UpdateChannelRequest request);
//
//    void deleteChannel(Long channelId);
//
//    // Self-service join — only works for non-private channels. Joining a
//    // private channel requires a group OWNER/ADMIN to add the user instead.
//    ChannelResponse.MemberInfo joinChannel(JoinChannelRequest request);
//
//    void leaveChannel(Long channelId);
//
//    void removeMember(Long channelId, Long userId);
//
//    // Invalidates the current invite code and issues a new one — for when
//    // a code has leaked or you simply want to close off the old link.
//    // Only callable by a channel ADMIN or group OWNER/ADMIN; throws if
//    // called on a non-private channel (there's nothing to regenerate).
//    ChannelResponse regenerateInviteCode(Long channelId);
//}
package com.maalfa.communication_platform.service;

import com.maalfa.communication_platform.dto.channel.ChannelResponse;
import com.maalfa.communication_platform.dto.channel.CreateChannelRequest;
import com.maalfa.communication_platform.dto.channel.JoinChannelRequest;
import com.maalfa.communication_platform.dto.channel.UpdateChannelRequest;

import java.util.List;

public interface ChannelService {

    ChannelResponse createChannel(CreateChannelRequest request);

    ChannelResponse getChannelById(Long channelId);

    // All channels within a group that the current user is allowed to see
    // (private channels are excluded unless the user is already a member).
    List<ChannelResponse> getChannelsByGroup(Long groupId);

    List<ChannelResponse.MemberInfo> getChannelMembers(Long channelId);

    ChannelResponse updateChannel(Long channelId, UpdateChannelRequest request);

    void deleteChannel(Long channelId);

    // Self-service join — only works for non-private channels. Joining a
    // private channel requires a group OWNER/ADMIN to add the user instead.
    ChannelResponse.MemberInfo joinChannel(JoinChannelRequest request);

    // Admin-driven add — for private channels, or public channels where an
    // admin wants to add someone rather than waiting for self-join. Caller
    // must be a channel ADMIN or group OWNER/ADMIN; target must already be
    // a member of the parent group.
    ChannelResponse.MemberInfo addMember(Long channelId, Long userId);

    void leaveChannel(Long channelId);

    void removeMember(Long channelId, Long userId);

    // Invalidates the current invite code and issues a new one — for when
    // a code has leaked or you simply want to close off the old link.
    // Only callable by a channel ADMIN or group OWNER/ADMIN; throws if
    // called on a non-private channel (there's nothing to regenerate).
    ChannelResponse regenerateInviteCode(Long channelId);
}
