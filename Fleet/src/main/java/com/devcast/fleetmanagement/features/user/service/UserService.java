package com.devcast.fleetmanagement.features.user.service;

import com.devcast.fleetmanagement.features.user.model.User;
import com.devcast.fleetmanagement.features.user.model.util.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * User Service Interface
 * Handles user management, authentication, and profile operations
 */
public interface UserService {

    // ==================== User CRUD Operations ====================

    /**
     * Create new user with role
     */
    User createUser(User user, Role role, Long companyId);

    /**
     * Get user by ID
     */
    Optional<User> getUserById(Long userId);

    /**
     * Get user by email
     */
    Optional<User> getUserByEmail(String email);

    /**
     * Update user profile
     */
    User updateUser(Long userId, User user);

    /**
     * Delete user
     */
    void deleteUser(Long userId);

    /**
     * Get all users in company
     */
    Page<User> getUsersByCompany(Long companyId, Pageable pageable);

    /**
     * Get users by role
     */
    List<User> getUsersByRole(Long companyId, Role role);

    /**
     * Get all active users in company
     */
    List<User> getActiveUsers(Long companyId);

    // ==================== User Role Management ====================

    /**
     * Assign role to user
     */
    void assignRole(Long userId, Role role);

    /**
     * Change user role
     */
    void changeUserRole(Long userId, Role newRole);

    /**
     * Get user role
     */
    Optional<Role> getUserRole(Long userId);

    /**
     * Check if user has role
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
     * Change user password
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * Reset password to temporary
     */
    String resetPassword(Long userId);

    /**
     * Set password (internal use)
     */
    void setPassword(Long userId, String password);

    /**
     * Check password validity
     */
    boolean validatePassword(Long userId, String password);

    /**
     * Force password change on next login
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
     */
    Page<User> searchUsers(Long companyId, String searchTerm, Pageable pageable);

    /**
     * Filter users by multiple criteria
     */
    Page<User> filterUsers(Long companyId, UserFilterCriteria criteria, Pageable pageable);

    // ==================== Bulk Operations ====================

    /**
     * Bulk create users
     */
    List<User> bulkCreateUsers(Long companyId, List<User> users, Role role);

    /**
     * Bulk update user status
     */
    void bulkUpdateStatus(List<Long> userIds, String status);

    /**
     * Export users
     */
    byte[] exportUsersToCSV(Long companyId);

    // Data Transfer Objects

    record UserPermissions(
            Long userId,
            String userName,
            Role role,
            List<String> permissions,
            List<String> modules
    ) {}

    record UserActivity(
            Long userId,
            String userName,
            String email,
            Long lastLogin,
            Long loginCount,
            String status
    ) {}

    record UserFilterCriteria(
            Role role,
            String status,
            String department,
            Long fromDate,
            Long toDate
    ) {}
}
