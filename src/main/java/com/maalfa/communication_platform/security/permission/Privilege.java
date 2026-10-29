package com.maalfa.communication_platform.security.permission;

/**
 * The fixed, compile-time-safe catalog of every privilege the system
 * supports. Distinct from entity.Privilege (the DB row) — this enum is
 * what a data seeder uses to populate entity.Privilege rows consistently
 * on startup, and what service code references instead of typing raw
 * strings like "BAN_USER" by hand.
 */
public enum Privilege {
    // User management
    MANAGE_USERS("Manage user accounts (enable/disable, edit any profile)"),
    BAN_USER("Ban or suspend a user"),

    // Group / channel management
    CREATE_GROUP("Create a new group"),
    DELETE_ANY_GROUP("Delete any group, not just ones the user owns"),
    MANAGE_CHANNEL("Create, update, or delete channels within a group"),

    // Moderation
    DELETE_ANY_MESSAGE("Delete any message, not just the user's own"),
    MODERATE_CHANNEL("Mute/kick members within a channel"),

    // Auditing
    VIEW_AUDIT_LOG("View system/moderation audit logs");

    private final String description;

    Privilege(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
