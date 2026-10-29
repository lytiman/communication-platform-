package com.maalfa.communication_platform.enums;

// Scoped to a single Group — independent of the global Role/Privilege system.
// A user can be OWNER of one group and a plain MEMBER of another.
public enum GroupRole {
    OWNER,
    ADMIN,
    MEMBER
}
