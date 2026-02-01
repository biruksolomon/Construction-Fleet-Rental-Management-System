package com.DevCast.Fleet_Management.model.util;

import java.util.*;

/**
 * Maps roles to their permissions (RBAC Matrix)
 * Defines which permissions each role has access to
 */
public class RolePermissionMap {
    private static final Map<Role, Set<Permission>> ROLE_PERMISSIONS = new HashMap<>();

    static {
        // OWNER - Full system access
        ROLE_PERMISSIONS.put(Role.OWNER, new HashSet<>(Arrays.asList(
                // User Management
                Permission.CREATE_USER, Permission.READ_USER, Permission.UPDATE_USER,
                Permission.DELETE_USER, Permission.MANAGE_USER_ROLES,

                // Company Management
                Permission.CREATE_COMPANY, Permission.READ_COMPANY, Permission.UPDATE_COMPANY,
                Permission.DELETE_COMPANY, Permission.MANAGE_COMPANY_SETTINGS,

                // Vehicle Management
                Permission.CREATE_VEHICLE, Permission.READ_VEHICLE, Permission.UPDATE_VEHICLE,
                Permission.DELETE_VEHICLE, Permission.VIEW_VEHICLE_USAGE,

                // Driver Management
                Permission.CREATE_DRIVER, Permission.READ_DRIVER, Permission.UPDATE_DRIVER,
                Permission.DELETE_DRIVER, Permission.MANAGE_DRIVER_WORK_LIMITS,

                // Rental Management
                Permission.CREATE_RENTAL, Permission.READ_RENTAL, Permission.UPDATE_RENTAL,
                Permission.DELETE_RENTAL, Permission.APPROVE_RENTAL,

                // Client Management
                Permission.CREATE_CLIENT, Permission.READ_CLIENT, Permission.UPDATE_CLIENT,
                Permission.DELETE_CLIENT,

                // GPS & Tracking
                Permission.VIEW_GPS_DATA, Permission.VIEW_VEHICLE_LOCATION,
                Permission.EXPORT_GPS_DATA,

                // Fuel Management
                Permission.CREATE_FUEL_LOG, Permission.READ_FUEL_LOG, Permission.UPDATE_FUEL_LOG,
                Permission.DELETE_FUEL_LOG, Permission.VIEW_FUEL_ANALYSIS,

                // Maintenance
                Permission.CREATE_MAINTENANCE, Permission.READ_MAINTENANCE,
                Permission.UPDATE_MAINTENANCE, Permission.DELETE_MAINTENANCE,

                // Invoicing & Billing
                Permission.CREATE_INVOICE, Permission.READ_INVOICE, Permission.UPDATE_INVOICE,
                Permission.DELETE_INVOICE, Permission.APPROVE_INVOICE,
                Permission.VIEW_FINANCIAL_REPORTS,

                // Payroll Management
                Permission.CREATE_PAYROLL, Permission.READ_PAYROLL, Permission.UPDATE_PAYROLL,
                Permission.DELETE_PAYROLL, Permission.APPROVE_PAYROLL,
                Permission.PROCESS_PAYROLL,

                // Audit & Reporting
                Permission.VIEW_AUDIT_LOG, Permission.EXPORT_REPORTS, Permission.VIEW_ANALYTICS,

                // System Administration
                Permission.MANAGE_PRICING_RULES, Permission.MANAGE_SYSTEM_CONFIG,
                Permission.VIEW_SYSTEM_HEALTH, Permission.MANAGE_BACKUPS
        )));

        // ADMIN - Administrative access
        ROLE_PERMISSIONS.put(Role.ADMIN, new HashSet<>(Arrays.asList(
                // User Management
                Permission.CREATE_USER, Permission.READ_USER, Permission.UPDATE_USER,
                Permission.MANAGE_USER_ROLES,

                // Company Management
                Permission.READ_COMPANY, Permission.UPDATE_COMPANY,
                Permission.MANAGE_COMPANY_SETTINGS,

                // Vehicle Management
                Permission.CREATE_VEHICLE, Permission.READ_VEHICLE, Permission.UPDATE_VEHICLE,
                Permission.DELETE_VEHICLE, Permission.VIEW_VEHICLE_USAGE,

                // Driver Management
                Permission.CREATE_DRIVER, Permission.READ_DRIVER, Permission.UPDATE_DRIVER,
                Permission.DELETE_DRIVER, Permission.MANAGE_DRIVER_WORK_LIMITS,

                // Rental Management
                Permission.CREATE_RENTAL, Permission.READ_RENTAL, Permission.UPDATE_RENTAL,
                Permission.DELETE_RENTAL,

                // Client Management
                Permission.CREATE_CLIENT, Permission.READ_CLIENT, Permission.UPDATE_CLIENT,
                Permission.DELETE_CLIENT,

                // Audit & Reporting
                Permission.VIEW_AUDIT_LOG, Permission.EXPORT_REPORTS
        )));

        // FLEET_MANAGER - Vehicle and driver management
        ROLE_PERMISSIONS.put(Role.FLEET_MANAGER, new HashSet<>(Arrays.asList(
                // Vehicle Management (Full)
                Permission.CREATE_VEHICLE, Permission.READ_VEHICLE, Permission.UPDATE_VEHICLE,
                Permission.DELETE_VEHICLE, Permission.VIEW_VEHICLE_USAGE,

                // Driver Management (Full)
                Permission.CREATE_DRIVER, Permission.READ_DRIVER, Permission.UPDATE_DRIVER,
                Permission.DELETE_DRIVER, Permission.MANAGE_DRIVER_WORK_LIMITS,

                // Rental Management
                Permission.CREATE_RENTAL, Permission.READ_RENTAL, Permission.UPDATE_RENTAL,
                Permission.APPROVE_RENTAL,

                // Client Management (Read-only)
                Permission.READ_CLIENT,

                // GPS & Tracking
                Permission.VIEW_GPS_DATA, Permission.VIEW_VEHICLE_LOCATION,

                // Fuel Management
                Permission.CREATE_FUEL_LOG, Permission.READ_FUEL_LOG,
                Permission.VIEW_FUEL_ANALYSIS,

                // Maintenance
                Permission.CREATE_MAINTENANCE, Permission.READ_MAINTENANCE,
                Permission.UPDATE_MAINTENANCE,

                // Reporting
                Permission.VIEW_ANALYTICS, Permission.EXPORT_REPORTS
        )));

        // ACCOUNTANT - Financial and payroll access
        ROLE_PERMISSIONS.put(Role.ACCOUNTANT, new HashSet<>(Arrays.asList(
                // Invoicing & Billing (Full)
                Permission.CREATE_INVOICE, Permission.READ_INVOICE, Permission.UPDATE_INVOICE,
                Permission.DELETE_INVOICE, Permission.APPROVE_INVOICE,
                Permission.VIEW_FINANCIAL_REPORTS,

                // Payroll Management (Full)
                Permission.CREATE_PAYROLL, Permission.READ_PAYROLL, Permission.UPDATE_PAYROLL,
                Permission.DELETE_PAYROLL, Permission.APPROVE_PAYROLL,
                Permission.PROCESS_PAYROLL,

                // Rental Management (Read-only)
                Permission.READ_RENTAL,

                // Fuel Management (Read-only)
                Permission.READ_FUEL_LOG, Permission.VIEW_FUEL_ANALYSIS,

                // Vehicle/Driver (Read-only)
                Permission.READ_VEHICLE, Permission.READ_DRIVER,

                // Client Management (Read-only)
                Permission.READ_CLIENT,

                // Audit & Reporting
                Permission.VIEW_AUDIT_LOG, Permission.EXPORT_REPORTS,
                Permission.VIEW_ANALYTICS
        )));

        // DRIVER - Limited access
        ROLE_PERMISSIONS.put(Role.DRIVER, new HashSet<>(Arrays.asList(
                // Own data access
                Permission.READ_VEHICLE,
                Permission.READ_DRIVER,

                // Fuel Management (Can create logs)
                Permission.CREATE_FUEL_LOG, Permission.READ_FUEL_LOG,

                // Maintenance (Read-only)
                Permission.READ_MAINTENANCE,

                // GPS Data (View own)
                Permission.VIEW_VEHICLE_LOCATION,

                // Limited reporting
                Permission.VIEW_ANALYTICS
        )));
    }

    /**
     * Get all permissions for a specific role
     */
    public static Set<Permission> getPermissionsForRole(Role role) {
        return ROLE_PERMISSIONS.getOrDefault(role, new HashSet<>());
    }

    /**
     * Check if a role has a specific permission
     */
    public static boolean hasPermission(Role role, Permission permission) {
        Set<Permission> permissions = getPermissionsForRole(role);
        return permissions.contains(permission);
    }

    /**
     * Get all roles that have a specific permission
     */
    public static Set<Role> getRolesWithPermission(Permission permission) {
        Set<Role> roles = new HashSet<>();
        for (Map.Entry<Role, Set<Permission>> entry : ROLE_PERMISSIONS.entrySet()) {
            if (entry.getValue().contains(permission)) {
                roles.add(entry.getKey());
            }
        }
        return roles;
    }

    /**
     * Get permission count for a role
     */
    public static int getPermissionCountForRole(Role role) {
        return getPermissionsForRole(role).size();
    }

    /**
     * Get all roles with their permission counts
     */
    public static Map<Role, Integer> getAllRolePermissionCounts() {
        Map<Role, Integer> counts = new HashMap<>();
        for (Role role : Role.values()) {
            counts.put(role, getPermissionCountForRole(role));
        }
        return counts;
    }
}
