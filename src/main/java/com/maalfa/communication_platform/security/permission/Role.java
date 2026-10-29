package com.maalfa.communication_platform.security.permission;

import java.util.EnumSet;
import java.util.Set;

/**
 * The fixed, compile-time-safe catalog of every role the system supports,
 * each with its DEFAULT privilege bundle. Distinct from entity.Role (the
 * DB row, which can be edited at runtime) — this enum is what a data
 * seeder reads to create entity.Role rows and link them to entity.Privilege
 * rows automatically on first startup.
 */
public enum Role {

    ROLE_USER(EnumSet.noneOf(Privilege.class)),

    ROLE_MODERATOR(EnumSet.of(
            Privilege.DELETE_ANY_MESSAGE,
            Privilege.MODERATE_CHANNEL
    )),

    ROLE_ADMIN(EnumSet.of(
            Privilege.MANAGE_USERS,
            Privilege.BAN_USER,
            Privilege.CREATE_GROUP,
            Privilege.DELETE_ANY_GROUP,
            Privilege.MANAGE_CHANNEL,
            Privilege.DELETE_ANY_MESSAGE,
            Privilege.MODERATE_CHANNEL,
            Privilege.VIEW_AUDIT_LOG
    ));

    private final Set<Privilege> defaultPrivileges;

    Role(Set<Privilege> defaultPrivileges) {
        this.defaultPrivileges = defaultPrivileges;
    }

    public Set<Privilege> getDefaultPrivileges() {
        return defaultPrivileges;
    }
}
