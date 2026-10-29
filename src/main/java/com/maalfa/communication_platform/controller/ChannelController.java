//package com.maalfa.communication_platform.controller;
//
//import com.maalfa.communication_platform.dto.channel.ChannelResponse;
//import com.maalfa.communication_platform.dto.channel.CreateChannelRequest;
//import com.maalfa.communication_platform.dto.channel.JoinChannelRequest;
//import com.maalfa.communication_platform.dto.channel.UpdateChannelRequest;
//import com.maalfa.communication_platform.service.ChannelService;
//import jakarta.validation.Valid;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/channels")
//public class ChannelController {
//
//    private final ChannelService channelService;
//
//    public ChannelController(ChannelService channelService) {
//        this.channelService = channelService;
//    }
//
//    @PostMapping
//    public ResponseEntity<ChannelResponse> createChannel(@Valid @RequestBody CreateChannelRequest request) {
//        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.createChannel(request));
//    }
//
//    @GetMapping("/{channelId}")
//    public ResponseEntity<ChannelResponse> getChannelById(@PathVariable Long channelId) {
//        return ResponseEntity.ok(channelService.getChannelById(channelId));
//    }
//
//    @GetMapping("/group/{groupId}")
//    public ResponseEntity<List<ChannelResponse>> getChannelsByGroup(@PathVariable Long groupId) {
//        return ResponseEntity.ok(channelService.getChannelsByGroup(groupId));
//    }
//
//    @GetMapping("/{channelId}/members")
//    public ResponseEntity<List<ChannelResponse.MemberInfo>> getChannelMembers(@PathVariable Long channelId) {
//        return ResponseEntity.ok(channelService.getChannelMembers(channelId));
//    }
//
//    @PatchMapping("/{channelId}")
//    public ResponseEntity<ChannelResponse> updateChannel(@PathVariable Long channelId,
//                                                           @Valid @RequestBody UpdateChannelRequest request) {
//        return ResponseEntity.ok(channelService.updateChannel(channelId, request));
//    }
//
//    @DeleteMapping("/{channelId}")
//    public ResponseEntity<Void> deleteChannel(@PathVariable Long channelId) {
//        channelService.deleteChannel(channelId);
//        return ResponseEntity.noContent().build();
//    }
//
//    @PostMapping("/join")
//    public ResponseEntity<ChannelResponse.MemberInfo> joinChannel(@Valid @RequestBody JoinChannelRequest request) {
//        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.joinChannel(request));
//    }
//
//    @PostMapping("/{channelId}/leave")
//    public ResponseEntity<Void> leaveChannel(@PathVariable Long channelId) {
//        channelService.leaveChannel(channelId);
//        return ResponseEntity.noContent().build();
//    }
//
//    @DeleteMapping("/{channelId}/members/{userId}")
//    public ResponseEntity<Void> removeMember(@PathVariable Long channelId, @PathVariable Long userId) {
//        channelService.removeMember(channelId, userId);
//        return ResponseEntity.noContent().build();
//    }
//
//    @PostMapping("/{channelId}/invite-code/regenerate")
//    public ResponseEntity<ChannelResponse> regenerateInviteCode(@PathVariable Long channelId) {
//        return ResponseEntity.ok(channelService.regenerateInviteCode(channelId));
//    }
//}


package com.maalfa.communication_platform.controller;

import com.maalfa.communication_platform.dto.channel.ChannelResponse;
import com.maalfa.communication_platform.dto.channel.CreateChannelRequest;
import com.maalfa.communication_platform.dto.channel.JoinChannelRequest;
import com.maalfa.communication_platform.dto.channel.UpdateChannelRequest;
import com.maalfa.communication_platform.service.ChannelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @PostMapping
    public ResponseEntity<ChannelResponse> createChannel(@Valid @RequestBody CreateChannelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.createChannel(request));
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> getChannelById(@PathVariable Long channelId) {
        return ResponseEntity.ok(channelService.getChannelById(channelId));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ChannelResponse>> getChannelsByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(channelService.getChannelsByGroup(groupId));
    }

    @GetMapping("/{channelId}/members")
    public ResponseEntity<List<ChannelResponse.MemberInfo>> getChannelMembers(@PathVariable Long channelId) {
        return ResponseEntity.ok(channelService.getChannelMembers(channelId));
    }

    @PatchMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> updateChannel(@PathVariable Long channelId,
                                                         @Valid @RequestBody UpdateChannelRequest request) {
        return ResponseEntity.ok(channelService.updateChannel(channelId, request));
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long channelId) {
        channelService.deleteChannel(channelId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/join")
    public ResponseEntity<ChannelResponse.MemberInfo> joinChannel(@Valid @RequestBody JoinChannelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.joinChannel(request));
    }

    @PostMapping("/{channelId}/members/{userId}")
    public ResponseEntity<ChannelResponse.MemberInfo> addMember(@PathVariable Long channelId,
                                                                @PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.addMember(channelId, userId));
    }

    @PostMapping("/{channelId}/leave")
    public ResponseEntity<Void> leaveChannel(@PathVariable Long channelId) {
        channelService.leaveChannel(channelId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{channelId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long channelId, @PathVariable Long userId) {
        channelService.removeMember(channelId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{channelId}/invite-code/regenerate")
    public ResponseEntity<ChannelResponse> regenerateInviteCode(@PathVariable Long channelId) {
        return ResponseEntity.ok(channelService.regenerateInviteCode(channelId));
    }
}
