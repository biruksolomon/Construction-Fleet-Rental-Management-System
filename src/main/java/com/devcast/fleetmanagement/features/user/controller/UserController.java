package com.devcast.fleetmanagement.features.user.controller;

import com.devcast.fleetmanagement.features.auth.dto.ApiResponse;
import com.devcast.fleetmanagement.features.user.dto.*;
import com.devcast.fleetmanagement.features.user.model.User;
import com.devcast.fleetmanagement.features.user.model.util.Role;
import com.devcast.fleetmanagement.features.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Management REST Controller
 *
 * Endpoints for managing users, roles, permissions, and authentication
 * All endpoints require authentication and RBAC authorization
 *
 * Base Path: /users (context path /api is already set)
 * Multi-tenant: Enforced at service level via company context
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management, authentication, and profile endpoints")
public class UserController {

    private final UserService userService;

    // ==================== User CRUD Operations ====================

    /**
     * Create a new user
     * POST /api/users
     *
     * Request Body: UserCreateRequest (no id, timestamps, passwordHash)
     * Response: UserResponse (complete user information)
     * RBAC: OWNER, ADMIN only
     */
    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user in the company (OWNER, ADMIN only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @RequestParam Long companyId,
            @Valid @RequestBody UserCreateRequest request
    ) {
        try {
            log.info("Creating user: {} for company: {}", request.getEmail(), companyId);
            UserResponse created = userService.createUser(companyId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(created, "User created successfully"));
        } catch (SecurityException e) {
            log.warn("Security error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Insufficient permissions: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Validation error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Validation error: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create user: " + e.getMessage()));
        }
    }

    /**
     * Get user by ID
     * GET /api/users/{userId}
     *
     * Response: UserResponse (complete user details)
     * RBAC: Self or OWNER/ADMIN
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user", description = "Get user details by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable Long userId
    ) {
        try {
            return userService.getUserById(userId)
                    .map(user -> ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully")))
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        } catch (Exception e) {
            log.error("Error retrieving user {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve user: " + e.getMessage()));
        }
    }

    /**
     * Get users by company with pagination
     * GET /api/users/company/{companyId}
     *
     * Response: Page<UserResponse> (paginated user list)
     * RBAC: Multi-tenant check enforced
     */
    @GetMapping("/company/{companyId}")
    @Operation(summary = "List users by company", description = "Get paginated list of users in a company")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByCompany(
            @PathVariable Long companyId,
            Pageable pageable
    ) {
        try {
            Page<UserResponse> users = userService.getUsersByCompany(companyId, pageable);
            return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        } catch (Exception e) {
            log.error("Error retrieving users for company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    /**
     * Get users by role
     * GET /api/users/company/{companyId}/role/{role}
     *
     * Response: List<UserResponse> (filtered by role)
     */
    @GetMapping("/company/{companyId}/role/{role}")
    @Operation(summary = "Get users by role", description = "Get all users with specific role in company")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @PathVariable Long companyId,
            @PathVariable Role role
    ) {
        try {
            List<UserResponse> users = userService.getUsersByRole(companyId, role);
            return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        } catch (Exception e) {
            log.error("Error retrieving users by role", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    /**
     * Get all active users in company
     * GET /api/users/company/{companyId}/active
     *
     * Response: List<UserResponse> (active users only)
     */
    @GetMapping("/company/{companyId}/active")
    @Operation(summary = "Get active users", description = "Get all active users in company")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveUsers(
            @PathVariable Long companyId
    ) {
        try {
            List<UserResponse> users = userService.getActiveUsers(companyId);
            return ResponseEntity.ok(ApiResponse.success(users, "Active users retrieved successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        } catch (Exception e) {
            log.error("Error retrieving active users", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    /**
     * Update user
     * PUT /api/users/{userId}
     *
     * Request Body: UserUpdateRequest (all fields optional)
     * Response: UserResponse (updated user information)
     * RBAC: Self or OWNER/ADMIN
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Update user profile (self or ADMIN)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        try {
            log.info("Updating user: {}", userId);
            UserResponse updated = userService.updateUser(userId, request);
            return ResponseEntity.ok(ApiResponse.success(updated, "User updated successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Insufficient permissions"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Validation error: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating user {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update user: " + e.getMessage()));
        }
    }

    /**
     * Delete user
     * DELETE /api/users/{userId}
     *
     * RBAC: OWNER, ADMIN only
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Delete user permanently (OWNER, ADMIN only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "User deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId
    ) {
        try {
            log.info("Deleting user: {}", userId);
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            log.warn("Security error deleting user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error deleting user {}", userId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== Role Management ====================

    /**
     * Assign role to user
     * PUT /api/users/{userId}/role
     *
     * Request Body: UpdateRoleRequest (new role and reason)
     * RBAC: OWNER, ADMIN only
     */
    @PutMapping("/{userId}/role")
    @Operation(summary = "Update user role", description = "Change user role (OWNER, ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        try {
            log.info("Updating role for user: {}", userId);
            userService.assignRole(userId, request);
            return ResponseEntity.ok(ApiResponse.success(null, "User role updated successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Insufficient permissions"));
        } catch (Exception e) {
            log.error("Error updating user role {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update role: " + e.getMessage()));
        }
    }

    /**
     * Get user role
     * GET /api/users/{userId}/role
     */
    @GetMapping("/{userId}/role")
    @Operation(summary = "Get user role", description = "Get current user role")
    public ResponseEntity<ApiResponse<Role>> getUserRole(
            @PathVariable Long userId
    ) {
        try {
            return userService.getUserRole(userId)
                    .map(role -> ResponseEntity.ok(ApiResponse.success(role, "User role retrieved")))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving user role {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve role: " + e.getMessage()));
        }
    }

    // ==================== User Status Management ====================

    /**
     * Activate user
     * PUT /api/users/{userId}/activate
     *
     * RBAC: OWNER, ADMIN only
     */
    @PutMapping("/{userId}/activate")
    @Operation(summary = "Activate user", description = "Activate user account (OWNER, ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> activateUser(
            @PathVariable Long userId
    ) {
        try {
            userService.activateUser(userId);
            return ResponseEntity.ok(ApiResponse.success(null, "User activated successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Insufficient permissions"));
        } catch (Exception e) {
            log.error("Error activating user {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to activate user: " + e.getMessage()));
        }
    }

    /**
     * Deactivate user
     * PUT /api/users/{userId}/deactivate
     *
     * RBAC: OWNER, ADMIN only
     */
    @PutMapping("/{userId}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivate user account (OWNER, ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @PathVariable Long userId
    ) {
        try {
            userService.deactivateUser(userId);
            return ResponseEntity.ok(ApiResponse.success(null, "User deactivated successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Insufficient permissions"));
        } catch (Exception e) {
            log.error("Error deactivating user {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to deactivate user: " + e.getMessage()));
        }
    }

    // ==================== Password Management ====================

    /**
     * Change password
     * POST /api/users/{userId}/change-password
     *
     * Request Body: ChangePasswordRequest (current and new passwords)
     * RBAC: Self or ADMIN
     */
    @PostMapping("/{userId}/change-password")
    @Operation(summary = "Change password", description = "Change user password with verification")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        try {
            userService.changePassword(userId, request);
            return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
        } catch (SecurityException e) {
            log.warn("Security error changing password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Invalid current password or insufficient permissions"));
        } catch (Exception e) {
            log.error("Error changing password for user {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to change password: " + e.getMessage()));
        }
    }

    /**
     * Reset password
     * POST /api/users/{userId}/reset-password
     *
     * Returns temporary password
     * RBAC: OWNER, ADMIN only
     */
    @PostMapping("/{userId}/reset-password")
    @Operation(summary = "Reset password", description = "Reset user password to temporary (OWNER, ADMIN only)")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @PathVariable Long userId
    ) {
        try {
            String temporaryPassword = userService.resetPassword(userId);
            return ResponseEntity.ok(ApiResponse.success(temporaryPassword,
                    "Password reset successfully. Temporary password: " + temporaryPassword));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Insufficient permissions"));
        } catch (Exception e) {
            log.error("Error resetting password for user {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to reset password: " + e.getMessage()));
        }
    }

    // ==================== Permissions ====================

    /**
     * Get user permissions
     * GET /api/users/{userId}/permissions
     *
     * Response: UserPermissions (detailed permissions)
     * RBAC: Self or ADMIN
     */
    @GetMapping("/{userId}/permissions")
    @Operation(summary = "Get user permissions", description = "Get all user permissions and modules")
    public ResponseEntity<ApiResponse<UserPermissions>> getUserPermissions(
            @PathVariable Long userId
    ) {
        try {
            UserPermissions permissions = userService.getUserPermissionsDetailed(userId);
            return ResponseEntity.ok(ApiResponse.success(permissions, "Permissions retrieved successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        } catch (Exception e) {
            log.error("Error retrieving permissions for user {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve permissions: " + e.getMessage()));
        }
    }

    // ==================== Search & Filter ====================

    /**
     * Search users
     * GET /api/users/search?q=term&companyId=id
     *
     * Response: Page<UserResponse> (matching users)
     */
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by name or email")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @RequestParam Long companyId,
            @RequestParam String searchTerm,
            Pageable pageable
    ) {
        try {
            Page<UserResponse> users = userService.searchUsers(companyId, searchTerm, pageable);
            return ResponseEntity.ok(ApiResponse.success(users, "Users found"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        } catch (Exception e) {
            log.error("Error searching users", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }

    /**
     * Filter users
     * POST /api/users/filter
     *
     * Request Body: UserFilterCriteria (filtering parameters)
     * Response: Page<UserResponse> (filtered users)
     */
    @PostMapping("/filter")
    @Operation(summary = "Filter users", description = "Filter users by multiple criteria")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> filterUsers(
            @RequestParam Long companyId,
            @RequestBody UserFilterCriteria criteria,
            Pageable pageable
    ) {
        try {
            Page<UserResponse> users = userService.filterUsers(companyId, criteria, pageable);
            return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        } catch (Exception e) {
            log.error("Error filtering users", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Filter failed: " + e.getMessage()));
        }
    }

    // ==================== Bulk Operations ====================

    /**
     * Bulk create users
     * POST /api/users/bulk
     *
     * Request Body: List<UserCreateRequest> (users to create)
     * Response: List<UserResponse> (created users)
     * RBAC: OWNER, ADMIN only
     */
    @PostMapping("/bulk")
    @Operation(summary = "Bulk create users", description = "Create multiple users at once (OWNER, ADMIN only)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> bulkCreateUsers(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "DRIVER") Role role,
            @Valid @RequestBody List<UserCreateRequest> requests
    ) {
        try {
            log.info("Bulk creating {} users", requests.size());
            List<UserResponse> created = userService.bulkCreateUsers(companyId, requests, role);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(created, "Users created: " + created.size()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Insufficient permissions"));
        } catch (Exception e) {
            log.error("Error bulk creating users", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Bulk operation failed: " + e.getMessage()));
        }
    }

    /**
     * Export users to CSV
     * GET /api/users/export?companyId=id
     *
     * Response: byte[] (CSV file)
     * RBAC: OWNER, ADMIN only
     */
    @GetMapping("/export")
    @Operation(summary = "Export users", description = "Export users to CSV format (OWNER, ADMIN only)")
    public ResponseEntity<byte[]> exportUsers(
            @RequestParam Long companyId
    ) {
        try {
            log.info("Exporting users for company: {}", companyId);
            byte[] csvData = userService.exportUsersToCSV(companyId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(csvData);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error exporting users", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
