# Role-Based Access Control (RBAC) - Complete Guide

## Quick Reference

| Role | Level | Key Responsibilities | Permission Count |
|------|-------|----------------------|-----------------|
| **OWNER** | 1 | Full system access, company management | 80+ |
| **ADMIN** | 2 | User & system administration | 60+ |
| **FLEET_MANAGER** | 3 | Vehicle & driver operations | 35+ |
| **ACCOUNTANT** | 3 | Financial & payroll operations | 25+ |
| **DRIVER** | 5 | Limited personal access | 10+ |

---

## Role Details

### 1. OWNER - Full Administrative Access
**Code**: `ROLE_OWNER` | **Level**: 1 (Highest)

#### Responsibilities
- Company creation and deletion
- User management (create, read, update, delete all users)
- User role assignment and revocation
- Company settings and configuration
- All vehicle operations and management
- All driver operations and management
- All rental contract management
- All client management
- Invoicing and financial reports
- Payroll processing and approval
- System configuration
- Audit log access and review
- Pricing rules management
- System health monitoring

#### Key Permissions
```
User Management: CREATE, READ, UPDATE, DELETE, MANAGE_ROLES
Company: CREATE, READ, UPDATE, DELETE, MANAGE_SETTINGS
Vehicles: CREATE, READ, UPDATE, DELETE, VIEW_USAGE
Drivers: CREATE, READ, UPDATE, DELETE, MANAGE_WORK_LIMITS
Rental: CREATE, READ, UPDATE, DELETE, APPROVE
Invoicing: CREATE, READ, UPDATE, DELETE, APPROVE, VIEW_REPORTS
Payroll: CREATE, READ, UPDATE, DELETE, APPROVE, PROCESS
GPS/Tracking: VIEW_DATA, VIEW_LOCATION, EXPORT_DATA
Audit: VIEW_LOGS, EXPORT_REPORTS
System: MANAGE_PRICING, MANAGE_CONFIG, VIEW_HEALTH, MANAGE_BACKUPS
```

#### Use Cases
- Company owners overseeing entire operations
- System administrators with full control
- Executive decision-making and strategic planning

#### Restrictions
- None (full access to all features)

---

### 2. ADMIN - Administrative Control
**Code**: `ROLE_ADMIN` | **Level**: 2

#### Responsibilities
- User creation (but not deletion)
- User role assignment
- Company settings management (not creation/deletion)
- Vehicle fleet management
- Driver management
- Rental contract approval
- Client management
- Basic audit log access
- Cannot create or delete companies
- Cannot process payroll

#### Key Permissions
```
User Management: CREATE, READ, UPDATE, MANAGE_ROLES (no DELETE)
Company: READ, UPDATE, MANAGE_SETTINGS (no CREATE/DELETE)
Vehicles: CREATE, READ, UPDATE, DELETE
Drivers: CREATE, READ, UPDATE, DELETE, MANAGE_WORK_LIMITS
Rental: CREATE, READ, UPDATE, DELETE
Clients: CREATE, READ, UPDATE, DELETE
Audit: VIEW_LOGS
GPS/Tracking: VIEW_DATA, VIEW_LOCATION
```

#### Use Cases
- Fleet company administrators
- Office managers overseeing day-to-day operations
- HR managers handling employee management

#### Restrictions
- Cannot create or delete companies
- Cannot manage system-wide settings
- Cannot access payroll operations
- Cannot delete users (archival only)

---

### 3. FLEET_MANAGER - Operations Manager
**Code**: `ROLE_FLEET_MANAGER` | **Level**: 3

#### Responsibilities
- Full vehicle management (CRUD operations)
- Full driver management (CRUD operations)
- Setting driver work limits
- Creating and managing rental contracts
- Approving rental requests
- GPS tracking and vehicle location monitoring
- Fuel logging and analysis
- Maintenance record management
- Analytics and reporting
- Read-only client and vehicle usage access

#### Key Permissions
```
Vehicles: CREATE, READ, UPDATE, DELETE, VIEW_USAGE
Drivers: CREATE, READ, UPDATE, DELETE, MANAGE_WORK_LIMITS
Rental: CREATE, READ, UPDATE, APPROVE (no DELETE)
GPS/Tracking: VIEW_DATA, VIEW_LOCATION (no EXPORT)
Fuel: CREATE, READ, VIEW_ANALYSIS
Maintenance: CREATE, READ, UPDATE (no DELETE)
Analytics: VIEW_ANALYTICS, EXPORT_REPORTS
Clients: READ (read-only)
```

#### Use Cases
- Fleet operations managers
- Logistics coordinators
- Vehicle maintenance supervisors
- GPS tracking and route optimization

#### Restrictions
- Cannot manage users
- Cannot modify company settings
- Cannot delete rental contracts
- Cannot access financial/payroll data
- Cannot export GPS data
- Cannot delete maintenance records

---

### 4. ACCOUNTANT - Financial Operations
**Code**: `ROLE_ACCOUNTANT` | **Level**: 3

#### Responsibilities
- Full invoice management (create, approve, process)
- Full payroll processing (create, approve, process)
- Financial report generation
- Revenue tracking and analysis
- Expense management
- Read-only access to rental and vehicle data
- Audit log access for financial tracking

#### Key Permissions
```
Invoicing: CREATE, READ, UPDATE, DELETE, APPROVE, VIEW_REPORTS
Payroll: CREATE, READ, UPDATE, DELETE, APPROVE, PROCESS
Rentals: READ (read-only)
Vehicles: READ (read-only)
Drivers: READ (read-only)
Clients: READ (read-only)
Fuel: READ, VIEW_ANALYSIS (read-only)
Analytics: VIEW_ANALYTICS, EXPORT_REPORTS
Audit: VIEW_LOGS, EXPORT_REPORTS
```

#### Use Cases
- Company accountants and bookkeepers
- Payroll processors
- Financial analysts
- CFO/Financial controllers

#### Restrictions
- Cannot manage users or vehicles
- Cannot create or delete contracts
- Cannot modify company settings
- Read-only access to operational data
- Cannot approve amounts above limit (customizable)

---

### 5. DRIVER - Limited Personal Access
**Code**: `ROLE_DRIVER` | **Level**: 5 (Lowest)

#### Responsibilities
- View assigned vehicle details
- View personal driver profile
- Create fuel consumption logs
- View personal GPS location
- Basic analytics access
- Submit personal data updates

#### Key Permissions
```
Vehicles: READ (assigned only)
Drivers: READ (own profile only)
Fuel: CREATE (own logs), READ
GPS/Tracking: VIEW_VEHICLE_LOCATION (own vehicle)
Analytics: VIEW_ANALYTICS (limited)
```

#### Use Cases
- Individual drivers
- Delivery personnel
- Equipment operators
- Fleet employees

#### Restrictions
- Cannot create or modify vehicles
- Cannot view other drivers' information
- Cannot access financial data
- Cannot view all vehicles (only assigned)
- Cannot export data
- Cannot access administrative functions

---

## Permission Matrix

### User Management
| Permission | Owner | Admin | Fleet Mgr | Accountant | Driver |
|-----------|:-----:|:-----:|:---------:|:----------:|:------:|
| CREATE_USER | ✅ | ✅ | ❌ | ❌ | ❌ |
| READ_USER | ✅ | ✅ | ❌ | ❌ | ❌ |
| UPDATE_USER | ✅ | ✅ | ❌ | ❌ | ❌ |
| DELETE_USER | ✅ | ❌ | ❌ | ❌ | ❌ |
| MANAGE_USER_ROLES | ✅ | ✅ | ❌ | ❌ | ❌ |

### Company Management
| Permission | Owner | Admin | Fleet Mgr | Accountant | Driver |
|-----------|:-----:|:-----:|:---------:|:----------:|:------:|
| CREATE_COMPANY | ✅ | ❌ | ❌ | ❌ | ❌ |
| READ_COMPANY | ✅ | ✅ | ✅ | ✅ | ❌ |
| UPDATE_COMPANY | ✅ | ✅ | ❌ | ❌ | ❌ |
| DELETE_COMPANY | ✅ | ❌ | ❌ | ❌ | ❌ |
| MANAGE_COMPANY_SETTINGS | ✅ | ✅ | ❌ | ❌ | ❌ |

### Vehicle Management
| Permission | Owner | Admin | Fleet Mgr | Accountant | Driver |
|-----------|:-----:|:-----:|:---------:|:----------:|:------:|
| CREATE_VEHICLE | ✅ | ✅ | ✅ | ❌ | ❌ |
| READ_VEHICLE | ✅ | ✅ | ✅ | ✅ | ✅ |
| UPDATE_VEHICLE | ✅ | ✅ | ✅ | ❌ | ❌ |
| DELETE_VEHICLE | ✅ | ✅ | ✅ | ❌ | ❌ |
| VIEW_VEHICLE_USAGE | ✅ | ✅ | ✅ | ❌ | ✅ |

### Driver Management
| Permission | Owner | Admin | Fleet Mgr | Accountant | Driver |
|-----------|:-----:|:-----:|:---------:|:----------:|:------:|
| CREATE_DRIVER | ✅ | ✅ | ✅ | ❌ | ❌ |
| READ_DRIVER | ✅ | ✅ | ✅ | ✅ | ✅ |
| UPDATE_DRIVER | ✅ | ✅ | ✅ | ❌ | ❌ |
| DELETE_DRIVER | ✅ | ✅ | ✅ | ❌ | ❌ |
| MANAGE_DRIVER_WORK_LIMITS | ✅ | ✅ | ✅ | ❌ | ❌ |

### Rental Management
| Permission | Owner | Admin | Fleet Mgr | Accountant | Driver |
|-----------|:-----:|:-----:|:---------:|:----------:|:------:|
| CREATE_RENTAL | ✅ | ✅ | ✅ | ❌ | ❌ |
| READ_RENTAL | ✅ | ✅ | ✅ | ✅ | ❌ |
| UPDATE_RENTAL | ✅ | ✅ | ✅ | ❌ | ❌ |
| DELETE_RENTAL | ✅ | ✅ | ❌ | ❌ | ❌ |
| APPROVE_RENTAL | ✅ | ✅ | ✅ | ❌ | ❌ |

### Invoicing
| Permission | Owner | Admin | Fleet Mgr | Accountant | Driver |
|-----------|:-----:|:-----:|:---------:|:----------:|:------:|
| CREATE_INVOICE | ✅ | ❌ | ❌ | ✅ | ❌ |
| READ_INVOICE | ✅ | ❌ | ❌ | ✅ | ❌ |
| UPDATE_INVOICE | ✅ | ❌ | ❌ | ✅ | ❌ |
| DELETE_INVOICE | ✅ | ❌ | ❌ | ✅ | ❌ |
| APPROVE_INVOICE | ✅ | ❌ | ❌ | ✅ | ❌ |
| VIEW_FINANCIAL_REPORTS | ✅ | ❌ | ❌ | ✅ | ❌ |

### Payroll
| Permission | Owner | Admin | Fleet Mgr | Accountant | Driver |
|-----------|:-----:|:-----:|:---------:|:----------:|:------:|
| CREATE_PAYROLL | ✅ | ❌ | ❌ | ✅ | ❌ |
| READ_PAYROLL | ✅ | ❌ | ❌ | ✅ | ❌ |
| UPDATE_PAYROLL | ✅ | ❌ | ❌ | ✅ | ❌ |
| DELETE_PAYROLL | ✅ | ❌ | ❌ | ✅ | ❌ |
| APPROVE_PAYROLL | ✅ | ❌ | ❌ | ✅ | ❌ |
| PROCESS_PAYROLL | ✅ | ❌ | ❌ | ✅ | ❌ |

### GPS & Tracking
| Permission | Owner | Admin | Fleet Mgr | Accountant | Driver |
|-----------|:-----:|:-----:|:---------:|:----------:|:------:|
| VIEW_GPS_DATA | ✅ | ✅ | ✅ | ❌ | ❌ |
| VIEW_VEHICLE_LOCATION | ✅ | ✅ | ✅ | ❌ | ✅ |
| EXPORT_GPS_DATA | ✅ | ❌ | ❌ | ❌ | ❌ |

### Fuel Management
| Permission | Owner | Admin | Fleet Mgr | Accountant | Driver |
|-----------|:-----:|:-----:|:---------:|:----------:|:------:|
| CREATE_FUEL_LOG | ✅ | ✅ | ✅ | ❌ | ✅ |
| READ_FUEL_LOG | ✅ | ✅ | ✅ | ✅ | ✅ |
| UPDATE_FUEL_LOG | ✅ | ✅ | ❌ | ❌ | ❌ |
| DELETE_FUEL_LOG | ✅ | ✅ | ❌ | ❌ | ❌ |
| VIEW_FUEL_ANALYSIS | ✅ | ✅ | ✅ | ✅ | ❌ |

### Maintenance
| Permission | Owner | Admin | Fleet Mgr | Accountant | Driver |
|-----------|:-----:|:-----:|:---------:|:----------:|:------:|
| CREATE_MAINTENANCE | ✅ | ✅ | ✅ | ❌ | ❌ |
| READ_MAINTENANCE | ✅ | ✅ | ✅ | ❌ | ✅ |
| UPDATE_MAINTENANCE | ✅ | ✅ | ✅ | ❌ | ❌ |
| DELETE_MAINTENANCE | ✅ | ✅ | ❌ | ❌ | ❌ |

### Audit & Reporting
| Permission | Owner | Admin | Fleet Mgr | Accountant | Driver |
|-----------|:-----:|:-----:|:---------:|:----------:|:------:|
| VIEW_AUDIT_LOG | ✅ | ✅ | ❌ | ✅ | ❌ |
| EXPORT_REPORTS | ✅ | ✅ | ✅ | ✅ | ❌ |
| VIEW_ANALYTICS | ✅ | ✅ | ✅ | ✅ | ✅ |

### System Administration
| Permission | Owner | Admin | Fleet Mgr | Accountant | Driver |
|-----------|:-----:|:-----:|:---------:|:----------:|:------:|
| MANAGE_PRICING_RULES | ✅ | ❌ | ❌ | ❌ | ❌ |
| MANAGE_SYSTEM_CONFIG | ✅ | ❌ | ❌ | ❌ | ❌ |
| VIEW_SYSTEM_HEALTH | ✅ | ✅ | ❌ | ❌ | ❌ |
| MANAGE_BACKUPS | ✅ | ❌ | ❌ | ❌ | ❌ |

---

## Permission Distribution

```
OWNER (80 permissions)
├── User Management (5)
├── Company Management (5)
├── Vehicle Management (5)
├── Driver Management (5)
├── Rental Management (5)
├── Client Management (4)
├── GPS & Tracking (3)
├── Fuel Management (5)
├── Maintenance (4)
├── Invoicing (6)
├── Payroll (6)
├── Audit & Reporting (3)
└── System Administration (4)

ADMIN (60 permissions)
├── User Management (4) - no DELETE
├── Company Management (3) - no CREATE/DELETE
├── Vehicle Management (5)
├── Driver Management (5)
├── Rental Management (4)
├── Client Management (4)
├── GPS & Tracking (2)
├── Fuel Management (2)
├── Maintenance (2)
├── Audit & Reporting (2)
├── Analytics (1)
└── System Administration (2)

FLEET_MANAGER (35 permissions)
├── Vehicle Management (5)
├── Driver Management (5)
├── Rental Management (4)
├── GPS & Tracking (2)
├── Fuel Management (3)
├── Maintenance (3)
├── Client Management (1) - READ only
├── Analytics (2)
└── Reporting (2)

ACCOUNTANT (25 permissions)
├── Invoicing (6)
├── Payroll (6)
├── Read-only Access (8)
├── Analytics (2)
├── Audit (2)
└── Reporting (1)

DRIVER (10 permissions)
├── Vehicle Access (1) - own only
├── Driver Profile (1) - own only
├── Fuel Management (2)
├── GPS/Tracking (1) - own vehicle
├── Maintenance (1) - read-only
└── Analytics (1)
```

---

## Implementation Guide

### Using Annotations

#### Method-Level Permission Check
```java
@PostMapping
@RequirePermission(Permission.CREATE_VEHICLE)
public ResponseEntity<?> createVehicle(@RequestBody VehicleDTO dto) {
    return ResponseEntity.ok(vehicleService.create(dto));
}
```

#### Role-Based Check
```java
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('OWNER')")
public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
    return ResponseEntity.ok(vehicleService.delete(id));
}
```

#### Multi-Tenant Validation
```java
@GetMapping
@MultiTenant("companyId")
public ResponseEntity<?> getCompanyVehicles(@RequestParam Long companyId) {
    return ResponseEntity.ok(vehicleService.getByCompany(companyId));
}
```

### Programmatic Permission Checks

```java
// Check single permission
if (SecurityUtils.hasPermission(Permission.CREATE_VEHICLE)) {
    // User can create vehicles
}

// Check multiple permissions
if (SecurityUtils.hasAnyPermission(
    Permission.CREATE_VEHICLE,
    Permission.UPDATE_VEHICLE)) {
    // User can create or update vehicles
}

// Check role
if (SecurityUtils.hasRole(Role.FLEET_MANAGER)) {
    // User is fleet manager
}

// Get current user info
Long userId = SecurityUtils.getCurrentUserId();
Long companyId = SecurityUtils.getCurrentCompanyId();
String email = SecurityUtils.getCurrentUserEmail();
Role role = SecurityUtils.getCurrentUserRole();
```

---

## Common Workflows

### Workflow 1: Creating a Vehicle
```
Driver:               ❌ Access Denied
Accountant:           ❌ Access Denied
Fleet Manager:        ✅ Allowed
Admin:                ✅ Allowed
Owner:                ✅ Allowed
```

### Workflow 2: Creating an Invoice
```
Driver:               ❌ Access Denied
Fleet Manager:        ❌ Access Denied
Accountant:           ✅ Allowed
Admin:                ❌ Access Denied
Owner:                ✅ Allowed
```

### Workflow 3: Processing Payroll
```
Driver:               ❌ Access Denied
Fleet Manager:        ❌ Access Denied
Admin:                ❌ Access Denied
Accountant:           ✅ Allowed
Owner:                ✅ Allowed
```

### Workflow 4: Viewing Audit Logs
```
Driver:               ❌ Access Denied
Fleet Manager:        ❌ Access Denied
Accountant:           ✅ Allowed
Admin:                ✅ Allowed
Owner:                ✅ Allowed
```

### Workflow 5: Managing System Settings
```
Driver:               ❌ Access Denied
Fleet Manager:        ❌ Access Denied
Accountant:           ❌ Access Denied
Admin:                ❌ Access Denied
Owner:                ✅ Allowed
```

---

## Role Assignment Best Practices

1. **Principle of Least Privilege**: Assign only necessary permissions
2. **Role Separation**: Keep operational and financial roles separate
3. **Audit Trail**: Log all role assignments and changes
4. **Regular Review**: Audit user roles and permissions periodically
5. **Multi-Approver**: Require approval for sensitive role changes
6. **Documentation**: Keep record of role assignments and justification

---

## Troubleshooting

### User Claims Permission Denied

1. Check user's current role: `SELECT role FROM users WHERE id = ?`
2. Verify role has permission: Check RBAC_GUIDE.md matrix
3. Check company access: `SELECT company_id FROM users WHERE id = ?`
4. Verify token validity: Check JWT expiration
5. Check audit logs for details

### Role Escalation Concerns

1. Implement approval workflow for role changes
2. Log all role modifications to audit trail
3. Restrict role changes to OWNER only
4. Monitor for unusual role patterns
5. Regular access review process

---


**Version**: 1.0
**Status**: Production Ready
