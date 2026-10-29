package com.maalfa.communication_platform.enums;

// Scoped to a single Channel — lets a group admin demote themselves in one
// channel, or lets a group grant channel-specific moderators.
public enum ChannelRole {
    ADMIN,
    MODERATOR,
    MEMBER
}
