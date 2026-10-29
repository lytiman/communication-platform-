package com.maalfa.communication_platform.service;

import com.maalfa.communication_platform.dto.group.AddGroupMemberRequest;
import com.maalfa.communication_platform.dto.group.CreateGroupRequest;
import com.maalfa.communication_platform.dto.group.GroupResponse;

import java.util.List;

public interface GroupService {

    GroupResponse createGroup(CreateGroupRequest request);

    GroupResponse getGroupById(Long groupId);

    // Groups the current user is a member of.
    List<GroupResponse> getMyGroups();

    List<GroupResponse.MemberInfo> getGroupMembers(Long groupId);

    GroupResponse.MemberInfo addMember(Long groupId, AddGroupMemberRequest request);

    void removeMember(Long groupId, Long userId);

    // Current user leaves voluntarily. Blocked for the OWNER.
    void leaveGroup(Long groupId);

    // OWNER only.
    void deleteGroup(Long groupId);
}
