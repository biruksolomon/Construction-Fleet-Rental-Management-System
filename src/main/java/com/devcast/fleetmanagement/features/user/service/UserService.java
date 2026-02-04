package com.devcast.fleetmanagement.features.user.service;

import com.devcast.fleetmanagement.features.user.dto.*;
import com.devcast.fleetmanagement.features.user.model.User;
import com.devcast.fleetmanagement.features.user.model.util.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * User Service Interface (DTO-Based)
 *
 * Defines contract for user management operations using DTOs to separate
 * API contracts from entity persistence.
 *
 * Design Principles:
 * 1. All requests use *Request DTOs (no client-provided IDs, timestamps, or password hashes)
 * 2. All responses use *Response DTOs (complete user information without passwords)
 * 3. Service never exposes raw entities through API contracts
 * 4. RBAC checks performed in implementation
 *
 * Includes:
 * - User CRUD operations
 * - User role management
 * - User status management
 * - Password management
 * - Permission checks
 * - Activity tracking
 * - Search and filtering
 * - Bulk operations
 */
public interface UserService {

    // ==================== User CRUD Operations ====================

    /**
     * Create new user with role
     * Request: UserCreateRequest (no id, timestamps, passwordHash)
     * Response: UserResponse (complete user representation)
     * RBAC: OWNER, ADMIN
     */
    UserResponse createUser(Long companyId, UserCreateRequest request);

    /**
     * Get user by ID with multi-tenant check
     * Response: UserResponse (complete user details)
     * RBAC: User viewing own profile or OWNER/ADMIN
     */
    Optional<UserResponse> getUserById(Long userId);

    /**
     * Get user by email within company context
     * Response: UserResponse (complete user details)
     * RBAC: OWNER/ADMIN only
     */
    Optional<UserResponse> getUserByEmail(Long companyId, String email);

    /**
     * Update user profile
     * Request: UserUpdateRequest (all fields optional, no id/timestamps)
     * Response: UserResponse (updated user representation)
     * RBAC: User updating own profile or OWNER/ADMIN
     */
    UserResponse updateUser(Long userId, UserUpdateRequest request);

    /**
     * Delete user permanently
     * RBAC: OWNER, ADMIN only
     */
    void deleteUser(Long userId);

    /**
     * Get all users in company with pagination
     * Response: Page<UserResponse> (paginated user list)
     * RBAC: Multi-tenant check enforced
     */
    Page<UserResponse> getUsersByCompany(Long companyId, Pageable pageable);

    /**
     * Get users by specific role
     * Response: List<UserResponse> (filtered by role)
     * RBAC: Multi-tenant check enforced
     */
    List<UserResponse> getUsersByRole(Long companyId, Role role);

    /**
     * Get all active users in company
     * Response: List<UserResponse> (active users only)
     * RBAC: Multi-tenant check enforced
     */
    List<UserResponse> getActiveUsers(Long companyId);

    // ==================== User Role Management ====================

    /**
     * Assign role to user
     * Request: UpdateRoleRequest (new role and optional reason)
     * RBAC: OWNER, ADMIN only
     */
    void assignRole(Long userId, UpdateRoleRequest request);

    /**
     * Change user role with audit logging
     * Request: UpdateRoleRequest (new role and reason)
     * RBAC: OWNER only
     */
    void changeUserRole(Long userId, UpdateRoleRequest request);

    /**
     * Get user role
     * Response: Role enum value
     */
    Optional<Role> getUserRole(Long userId);

    /**
     * Check if user has specific role
     * Response: boolean
     */
    boolean hasRole(Long userId, Role role);

    // ==================== User Status Management ====================

    /**
     * Activate user account
     */
    void activateUser(Long userId);

    /**
     * Deactivate user account
     */
    void deactivateUser(Long userId);

    /**
     * Check if user is active
     */
    boolean isUserActive(Long userId);

    /**
     * Suspend user (temporary)
     */
    void suspendUser(Long userId, String reason);

    /**
     * Resume suspended user
     */
    void resumeUser(Long userId);

    // ==================== Password Management ====================

    /**
     * Change user password with current password verification
     * Request: ChangePasswordRequest (current and new passwords)
     * RBAC: User changing own password or ADMIN
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * Reset password to temporary (admin action)
     * Returns: Temporary password
     * RBAC: OWNER, ADMIN only
     */
    String resetPassword(Long userId);

    /**
     * Set password (internal use only)
     * RBAC: System/internal only
     */
    void setPassword(Long userId, String password);

    /**
     * Validate password against user account
     * Response: boolean
     */
    boolean validatePassword(Long userId, String password);

    /**
     * Force password change on next login
     * RBAC: OWNER, ADMIN only
     */
    void forcePasswordChange(Long userId);

    // ==================== User Permissions ====================

    /**
     * Get user permissions
     */
    List<String> getUserPermissions(Long userId);

    /**
     * Check if user has permission
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * Get all user permissions detailed
     */
    UserPermissions getUserPermissionsDetailed(Long userId);

    // ==================== User Activity ====================

    /**
     * Get user last login time
     */
    Optional<Long> getLastLoginTime(Long userId);

    /**
     * Update last login
     */
    void updateLastLogin(Long userId);

    /**
     * Get user activity count (logins in period)
     */
    Long getUserActivityCount(Long userId, Long fromDate, Long toDate);

    /**
     * Get most active users in company
     */
    List<UserActivity> getMostActiveUsers(Long companyId, int limit);

    // ==================== User Search & Filter ====================

    /**
     * Search users by name or email
     * Response: Page<UserResponse> (matching users)
     * RBAC: Multi-tenant check enforced
     */
    Page<UserResponse> searchUsers(Long companyId, String searchTerm, Pageable pageable);

    /**
     * Filter users by multiple criteria
     * Response: Page<UserResponse> (filtered users)
     * RBAC: Multi-tenant check enforced
     */
    Page<UserResponse> filterUsers(Long companyId, UserFilterCriteria criteria, Pageable pageable);

    // ==================== Bulk Operations ====================

    /**
     * Bulk create users from list
     * Response: List<UserResponse> (created users)
     * RBAC: OWNER, ADMIN only
     */
    List<UserResponse> bulkCreateUsers(Long companyId, List<UserCreateRequest> requests, Role role);

    /**
     * Bulk update user status
     * RBAC: OWNER, ADMIN only
     */
    void bulkUpdateStatus(List<Long> userIds, User.UserStatus status);

    /**
     * Export users to CSV format
     * Response: byte[] (CSV data)
     * RBAC: OWNER, ADMIN only
     */
    byte[] exportUsersToCSV(Long companyId);
}
