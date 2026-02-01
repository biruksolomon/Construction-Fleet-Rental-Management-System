package com.DevCast.Fleet_Management.model.util;

import lombok.Getter;

/**
 * Permission Enumeration for Fine-Grained Access Control
 * Each permission corresponds to a specific action in the system
 */
@Getter
public enum Permission {
    // User Management
    CREATE_USER("CREATE_USER", "Create new user"),
    READ_USER("READ_USER", "View user details"),
    UPDATE_USER("UPDATE_USER", "Update user information"),
    DELETE_USER("DELETE_USER", "Delete user account"),
    MANAGE_USER_ROLES("MANAGE_USER_ROLES", "Assign/revoke user roles"),

    // Company Management
    CREATE_COMPANY("CREATE_COMPANY", "Create new company"),
    READ_COMPANY("READ_COMPANY", "View company details"),
    UPDATE_COMPANY("UPDATE_COMPANY", "Update company information"),
    DELETE_COMPANY("DELETE_COMPANY", "Delete company"),
    MANAGE_COMPANY_SETTINGS("MANAGE_COMPANY_SETTINGS", "Manage company settings"),

    // Vehicle Management
    CREATE_VEHICLE("CREATE_VEHICLE", "Add new vehicle"),
    READ_VEHICLE("READ_VEHICLE", "View vehicle details"),
    UPDATE_VEHICLE("UPDATE_VEHICLE", "Update vehicle information"),
    DELETE_VEHICLE("DELETE_VEHICLE", "Delete vehicle"),
    VIEW_VEHICLE_USAGE("VIEW_VEHICLE_USAGE", "View vehicle usage metrics"),

    // Driver Management
    CREATE_DRIVER("CREATE_DRIVER", "Register new driver"),
    READ_DRIVER("READ_DRIVER", "View driver details"),
    UPDATE_DRIVER("UPDATE_DRIVER", "Update driver information"),
    DELETE_DRIVER("DELETE_DRIVER", "Delete driver"),
    MANAGE_DRIVER_WORK_LIMITS("MANAGE_DRIVER_WORK_LIMITS", "Set driver work limits"),

    // Rental Management
    CREATE_RENTAL("CREATE_RENTAL", "Create rental contract"),
    READ_RENTAL("READ_RENTAL", "View rental contract"),
    UPDATE_RENTAL("UPDATE_RENTAL", "Update rental contract"),
    DELETE_RENTAL("DELETE_RENTAL", "Cancel rental contract"),
    APPROVE_RENTAL("APPROVE_RENTAL", "Approve rental requests"),

    // Client Management
    CREATE_CLIENT("CREATE_CLIENT", "Register new client"),
    READ_CLIENT("READ_CLIENT", "View client details"),
    UPDATE_CLIENT("UPDATE_CLIENT", "Update client information"),
    DELETE_CLIENT("DELETE_CLIENT", "Delete client"),

    // GPS & Tracking
    VIEW_GPS_DATA("VIEW_GPS_DATA", "Access GPS tracking data"),
    VIEW_VEHICLE_LOCATION("VIEW_VEHICLE_LOCATION", "View real-time vehicle location"),
    EXPORT_GPS_DATA("EXPORT_GPS_DATA", "Export GPS tracking data"),

    // Fuel Management
    CREATE_FUEL_LOG("CREATE_FUEL_LOG", "Record fuel consumption"),
    READ_FUEL_LOG("READ_FUEL_LOG", "View fuel logs"),
    UPDATE_FUEL_LOG("UPDATE_FUEL_LOG", "Update fuel logs"),
    DELETE_FUEL_LOG("DELETE_FUEL_LOG", "Delete fuel logs"),
    VIEW_FUEL_ANALYSIS("VIEW_FUEL_ANALYSIS", "View fuel analysis reports"),

    // Maintenance
    CREATE_MAINTENANCE("CREATE_MAINTENANCE", "Create maintenance record"),
    READ_MAINTENANCE("READ_MAINTENANCE", "View maintenance records"),
    UPDATE_MAINTENANCE("UPDATE_MAINTENANCE", "Update maintenance records"),
    DELETE_MAINTENANCE("DELETE_MAINTENANCE", "Delete maintenance records"),

    // Invoicing & Billing
    CREATE_INVOICE("CREATE_INVOICE", "Create invoice"),
    READ_INVOICE("READ_INVOICE", "View invoice"),
    UPDATE_INVOICE("UPDATE_INVOICE", "Update invoice"),
    DELETE_INVOICE("DELETE_INVOICE", "Delete invoice"),
    APPROVE_INVOICE("APPROVE_INVOICE", "Approve invoice for payment"),
    VIEW_FINANCIAL_REPORTS("VIEW_FINANCIAL_REPORTS", "Access financial reports"),

    // Payroll Management
    CREATE_PAYROLL("CREATE_PAYROLL", "Create payroll record"),
    READ_PAYROLL("READ_PAYROLL", "View payroll records"),
    UPDATE_PAYROLL("UPDATE_PAYROLL", "Update payroll records"),
    DELETE_PAYROLL("DELETE_PAYROLL", "Delete payroll records"),
    APPROVE_PAYROLL("APPROVE_PAYROLL", "Approve payroll for processing"),
    PROCESS_PAYROLL("PROCESS_PAYROLL", "Execute payroll processing"),

    // Audit & Reporting
    VIEW_AUDIT_LOG("VIEW_AUDIT_LOG", "Access audit logs"),
    EXPORT_REPORTS("EXPORT_REPORTS", "Export system reports"),
    VIEW_ANALYTICS("VIEW_ANALYTICS", "View system analytics"),

    // System Administration
    MANAGE_PRICING_RULES("MANAGE_PRICING_RULES", "Configure pricing rules"),
    MANAGE_SYSTEM_CONFIG("MANAGE_SYSTEM_CONFIG", "Manage system configuration"),
    VIEW_SYSTEM_HEALTH("VIEW_SYSTEM_HEALTH", "Monitor system health"),
    MANAGE_BACKUPS("MANAGE_BACKUPS", "Manage system backups");

    private final String code;
    private final String description;

    Permission(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
