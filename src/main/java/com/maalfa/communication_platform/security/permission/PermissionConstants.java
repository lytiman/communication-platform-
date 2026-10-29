package com.maalfa.communication_platform.security.permission;

/**
 * String constants mirroring Privilege enum values exactly — needed
 * because @PreAuthorize("hasAuthority('...')") SpEL expressions read
 * more cleanly with a plain constant than an enum reference. Keep this
 * in sync with the Privilege enum; a mismatch here just means an
 * @PreAuthorize check silently never matches, so treat the enum as the
 * source of truth and these as its annotation-friendly mirror.
 *
 * Usage: @PreAuthorize("hasAuthority('" + PermissionConstants.BAN_USER + "')")
 * — Java concatenates this at compile time, so it's still a valid
 * constant expression inside the annotation.
 */
public final class PermissionConstants {

    private PermissionConstants() {
        // no instances
    }

    public static final String MANAGE_USERS = "MANAGE_USERS";
    public static final String BAN_USER = "BAN_USER";

    public static final String CREATE_GROUP = "CREATE_GROUP";
    public static final String DELETE_ANY_GROUP = "DELETE_ANY_GROUP";
    public static final String MANAGE_CHANNEL = "MANAGE_CHANNEL";

    public static final String DELETE_ANY_MESSAGE = "DELETE_ANY_MESSAGE";
    public static final String MODERATE_CHANNEL = "MODERATE_CHANNEL";

    public static final String VIEW_AUDIT_LOG = "VIEW_AUDIT_LOG";
}