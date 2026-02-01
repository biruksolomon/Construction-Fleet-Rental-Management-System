package com.DevCast.Fleet_Management.model.util;

/**
 * Role Enumeration for Role-Based Access Control (RBAC)
 * Defines all available roles in the Fleet Management System
 */
public enum Role {
    /**
     * Owner: Full system access, can manage company settings and all users
     */
    OWNER("ROLE_OWNER", "Company Owner"),

    /**
     * Admin: Administrative access, can manage users and system settings
     */
    ADMIN("ROLE_ADMIN", "System Administrator"),

    /**
     * Fleet Manager: Can manage vehicles, drivers, and rental contracts
     */
    FLEET_MANAGER("ROLE_FLEET_MANAGER", "Fleet Manager"),

    /**
     * Accountant: Access to invoicing, payroll, and financial reports
     */
    ACCOUNTANT("ROLE_ACCOUNTANT", "Accountant"),

    /**
     * Driver: Limited access, can view assigned vehicles and submit time logs
     */
    DRIVER("ROLE_DRIVER", "Driver");

    private final String authority;
    private final String displayName;

    Role(String authority, String displayName) {
        this.authority = authority;
        this.displayName = displayName;
    }

    public String getAuthority() {
        return authority;
    }

    public String getDisplayName() {
        return displayName;
    }
}
