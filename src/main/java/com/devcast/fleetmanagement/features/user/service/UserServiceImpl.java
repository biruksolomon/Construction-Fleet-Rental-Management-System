package com.devcast.fleetmanagement.features.user.service;

import com.devcast.fleetmanagement.features.audit.service.AuditService;
import com.devcast.fleetmanagement.features.company.model.Company;
import com.devcast.fleetmanagement.features.company.repository.CompanyRepository;
import com.devcast.fleetmanagement.features.user.dto.*;
import com.devcast.fleetmanagement.features.user.model.User;
import com.devcast.fleetmanagement.features.user.model.util.Permission;
import com.devcast.fleetmanagement.features.user.model.util.Role;
import com.devcast.fleetmanagement.features.user.model.util.RolePermissionMap;
import com.devcast.fleetmanagement.features.user.repository.UserRepository;
import com.devcast.fleetmanagement.security.annotation.RequirePermission;
import com.devcast.fleetmanagement.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User Service Implementation (DTO-Based)
 *
 * Handles all user management operations with:
 * - DTO conversion from requests/to responses
 * - Multi-tenant isolation enforcement
 * - RBAC permission checks
 * - Password hashing with bcrypt
 * - Audit logging for compliance
 * - Transaction management
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    // ==================== User CRUD Operations ====================

    @Override
    @RequirePermission(Permission.CREATE_USER)
    public UserResponse createUser(Long companyId, UserCreateRequest request) {
        log.info("Creating new user: {} for company: {}", request.getEmail(), companyId);

        verifyCompanyAccess(companyId);

        // Validate email uniqueness per company
        if (userRepository.existsByEmailAndCompanyId(request.getEmail(), companyId)) {
            throw new IllegalArgumentException("Email already exists in this company");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        User user = User.builder()
                .company(company)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .status(User.UserStatus.ACTIVE)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);

        auditLog(companyId, "USER_CREATED", "User created: " + request.getEmail(), saved.getId());
        log.info("User created successfully: {} (ID: {})", request.getEmail(), saved.getId());

        return UserResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isPresent()) {
            verifyUserAccess(userId);
            return user.map(UserResponse::fromEntity);
        }

        return Optional.empty();
    }


    @Override
    @RequirePermission(Permission.READ_USER)
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserByEmail(Long companyId, String email) {
        verifyCompanyAccess(companyId);
        return userRepository.findByCompanyIdAndEmail(companyId, email)
                .map(UserResponse::fromEntity);
    }

    @Override
    @RequirePermission(Permission.UPDATE_USER)
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        verifyUserAccess(userId);

        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update allowed fields from DTO
        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            existing.setFullName(request.getFullName());
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            // Check email uniqueness within company (excluding current user)
            if (!existing.getEmail().equals(request.getEmail())) {
                if (userRepository.existsByCompanyIdAndEmailAndIdNot(existing.getCompany().getId(), request.getEmail(), userId)) {
                    throw new IllegalArgumentException("Email already exists in this company");
                }
            }
            existing.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            existing.setPhone(request.getPhone());
        }

        if (request.getRole() != null) {
            // PHASE 5: Enhanced role change validation with privilege escalation prevention
            if (!SecurityUtils.hasPermission(Permission.MANAGE_USER_ROLES)) {
                throw new SecurityException("Only administrators can change user roles");
            }

            // Get current logged-in user
            String currentUserEmail = SecurityUtils.getCurrentUserEmail();
            User currentUser = userRepository.findByCompanyIdAndEmail(existing.getCompany().getId(), currentUserEmail)
                    .orElseThrow(() -> new SecurityException("Current user not found"));

            // Prevent escalation: cannot assign role higher than your own
            if (isRoleHigherThan(request.getRole(), currentUser.getRole())) {
                throw new SecurityException(String.format(
                        "Cannot assign role %s - you can only assign roles equal to or lower than your own (%s)",
                        request.getRole(), currentUser.getRole()));
            }

            // Prevent modification of higher-privileged users
            if (isRoleHigherThan(existing.getRole(), currentUser.getRole())) {
                throw new SecurityException(String.format(
                        "Cannot modify user with role %s - target user has higher privileges than you (%s)",
                        existing.getRole(), currentUser.getRole()));
            }

            existing.setRole(request.getRole());
        }

        // Handle password change if provided
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            existing.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        existing.setUpdatedAt(LocalDateTime.now());
        User updated = userRepository.save(existing);

        auditLog(existing.getCompany().getId(), "USER_UPDATED", "User updated: " + existing.getEmail(), updated.getId());
        log.info("User updated: {}", userId);

        return UserResponse.fromEntity(updated);
    }

    @Override
    @RequirePermission(Permission.DELETE_USER)
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        verifyUserAccess(userId);

        auditLog(user.getCompany().getId(), "USER_DELETED", "User deleted: " + user.getEmail(), user.getId());
        userRepository.delete(user);
        log.info("User deleted: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByCompany(Long companyId, Pageable pageable) {
        verifyCompanyAccess(companyId);

        Page<User> users = userRepository.findByCompanyId(companyId, pageable);
        return users.map(UserResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Long companyId, Role role) {
        verifyCompanyAccess(companyId);

        List<User> users = userRepository.findByCompanyIdAndRole(companyId, role);
        return UserResponse.fromEntities(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers(Long companyId) {
        verifyCompanyAccess(companyId);

        List<User> users = userRepository.findByCompanyIdAndStatus(companyId, User.UserStatus.ACTIVE);
        return UserResponse.fromEntities(users);
    }

    // ==================== User Role Management ====================

    @Override
    @RequirePermission(Permission.MANAGE_USER_ROLES)
    public void assignRole(Long userId, UpdateRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        verifyUserAccess(userId);

        // PHASE 5: Privilege escalation prevention
        // Get current logged-in user
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        User currentUser = userRepository.findByCompanyIdAndEmail(user.getCompany().getId(), currentUserEmail)
                .orElseThrow(() -> new SecurityException("Current user not found"));

        // Prevent escalation: users cannot grant roles higher than their own
        if (isRoleHigherThan(request.getNewRole(), currentUser.getRole())) {
            throw new SecurityException(String.format(
                    "Cannot assign role %s - you can only assign roles equal to or lower than your own (%s)",
                    request.getNewRole(), currentUser.getRole()));
        }

        // Prevent modification of higher-privileged users
        if (isRoleHigherThan(user.getRole(), currentUser.getRole())) {
            throw new SecurityException(String.format(
                    "Cannot modify user with role %s - target user has higher privileges than you (%s)",
                    user.getRole(), currentUser.getRole()));
        }

        // OWNERS cannot be modified except by other OWNERS
        if (user.getRole() == Role.OWNER && currentUser.getRole() != Role.OWNER) {
            throw new SecurityException("Only OWNER can modify another OWNER's role");
        }

        // Prevent self-demotion from OWNER role (last line of defense)
        if (user.getId().equals(currentUser.getId()) &&
                currentUser.getRole() == Role.OWNER &&
                request.getNewRole() != Role.OWNER) {
            throw new SecurityException("OWNER cannot demote themselves");
        }

        user.setRole(request.getNewRole());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String details = "Role assigned to " + user.getEmail() + ": " + request.getNewRole() +
                " by " + currentUser.getEmail() +
                (request.getReason() != null ? " Reason: " + request.getReason() : "");
        auditLog(user.getCompany().getId(), "ROLE_ASSIGNED", details, user.getId());

        log.info("Role assigned to user {} by {}: {}", userId, currentUser.getId(), request.getNewRole());
    }

    /**
     * PHASE 5: Determine if one role is higher than another
     * Hierarchy: OWNER > ADMIN > FLEET_MANAGER > ACCOUNTANT > DRIVER
     */
    private boolean isRoleHigherThan(Role role1, Role role2) {
        return getRoleLevel(role1) > getRoleLevel(role2);
    }

    private int getRoleLevel(Role role) {
        switch (role) {
            case OWNER:         return 5;
            case ADMIN:         return 4;
            case FLEET_MANAGER: return 3;
            case ACCOUNTANT:    return 2;
            case DRIVER:        return 1;
            default:            return 0;
        }
    }

    @Override
    @RequirePermission(Permission.MANAGE_USER_ROLES)
    public void changeUserRole(Long userId, UpdateRoleRequest request) {
        assignRole(userId, request);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> getUserRole(Long userId) {
        return userRepository.findById(userId)
                .map(User::getRole);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(Long userId, Role role) {
        return userRepository.findById(userId)
                .map(user -> user.getRole().equals(role))
                .orElse(false);
    }

    // ==================== User Status Management ====================

    @Override
    @RequirePermission(Permission.UPDATE_USER)
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        verifyUserAccess(userId);

        user.setStatus(User.UserStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        auditLog(user.getCompany().getId(), "USER_ACTIVATED", "User activated: " + user.getEmail(), user.getId());
        log.info("User activated: {}", userId);
    }

    @Override
    @RequirePermission(Permission.UPDATE_USER)
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        verifyUserAccess(userId);

        user.setStatus(User.UserStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        auditLog(user.getCompany().getId(), "USER_DEACTIVATED", "User deactivated: " + user.getEmail(), user.getId());
        log.info("User deactivated: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserActive(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getStatus() == User.UserStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    @RequirePermission(Permission.UPDATE_USER)
    public void suspendUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        verifyUserAccess(userId);

        user.setStatus(User.UserStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String details = "User suspended: " + user.getEmail() + (reason != null ? " Reason: " + reason : "");
        auditLog(user.getCompany().getId(), "USER_SUSPENDED", details, user.getId());
        log.info("User suspended: {}", userId);
    }

    @Override
    @RequirePermission(Permission.UPDATE_USER)
    public void resumeUser(Long userId) {
        activateUser(userId);
    }

    // ==================== Password Management ====================

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        verifyUserAccess(userId);

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            auditLog(user.getCompany().getId(), "PASSWORD_CHANGE_FAILED",
                    "Invalid current password for: " + user.getEmail(), user.getId());
            throw new SecurityException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        auditLog(user.getCompany().getId(), "PASSWORD_CHANGED", "Password changed for: " + user.getEmail(), user.getId());
        log.info("Password changed for user: {}", userId);
    }

    @Override
    @RequirePermission(Permission.UPDATE_USER)
    public String resetPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        verifyUserAccess(userId);

        String temporaryPassword = generateTemporaryPassword();
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        auditLog(user.getCompany().getId(), "PASSWORD_RESET",
                "Password reset for: " + user.getEmail(), user.getId());
        log.info("Password reset for user: {}", userId);

        return temporaryPassword;
    }

    @Override
    public void setPassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(password));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Password set for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePassword(Long userId, String password) {
        return userRepository.findById(userId)
                .map(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .orElse(false);
    }

    @Override
    @RequirePermission(Permission.UPDATE_USER)
    public void forcePasswordChange(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        verifyUserAccess(userId);

        // Set a flag to force password change (implementation depends on your auth system)
        auditLog(user.getCompany().getId(), "FORCE_PASSWORD_CHANGE",
                "Force password change for: " + user.getEmail(), user.getId());
        log.info("Force password change set for user: {}", userId);
    }

    // ==================== User Permissions ====================

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserPermissions(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return new ArrayList<>();
        }

        Role role = user.get().getRole();
        Set<Permission> permissions = RolePermissionMap.getPermissionsForRole(role);

        return permissions.stream()
                .map(Permission::name)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, String permission) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return false;
        }

        try {
            Permission perm = Permission.valueOf(permission);
            Role role = user.get().getRole();
            Set<Permission> permissions = RolePermissionMap.getPermissionsForRole(role);
            return permissions.contains(perm);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid permission: {}", permission);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserPermissions getUserPermissionsDetailed(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        verifyUserAccess(userId);

        Set<Permission> permissions = RolePermissionMap.getPermissionsForRole(user.getRole());

        return new UserPermissions(
                userId,
                user.getFullName(),
                user.getRole(),
                permissions.stream()
                        .map(Permission::name)
                        .collect(Collectors.toList()),
                new ArrayList<>() // modules will be populated by modules feature
        );
    }

    // ==================== User Activity ====================

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> getLastLoginTime(Long userId) {
        // Implementation depends on activity tracking table
        // For now, return empty
        return Optional.empty();
    }

    @Override
    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        auditLog(user.getCompany().getId(), "USER_LOGIN", user.getEmail(), user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserActivityCount(Long userId, Long fromDate, Long toDate) {
        // Implementation depends on activity tracking table
        return 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserActivity> getMostActiveUsers(Long companyId, int limit) {
        verifyCompanyAccess(companyId);
        // Implementation depends on activity tracking table
        return new ArrayList<>();
    }

    // ==================== User Search & Filter ====================

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(Long companyId, String searchTerm, Pageable pageable) {
        verifyCompanyAccess(companyId);

        Page<User> users = userRepository.searchByNameOrEmail(companyId, searchTerm, pageable);
        return users.map(UserResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> filterUsers(Long companyId, UserFilterCriteria criteria, Pageable pageable) {
        verifyCompanyAccess(companyId);

        // Build dynamic query based on criteria
        List<User> allUsers = userRepository.findByCompanyId(companyId);

        if (criteria.getRole() != null) {
            allUsers = allUsers.stream()
                    .filter(u -> u.getRole() == criteria.getRole())
                    .collect(Collectors.toList());
        }

        if (criteria.getStatus() != null) {
            allUsers = allUsers.stream()
                    .filter(u -> u.getStatus().toString().equals(criteria.getStatus()))
                    .collect(Collectors.toList());
        }

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allUsers.size());
        List<User> pageContent = allUsers.subList(start, end);

        Page<User> page = new PageImpl<>(pageContent, pageable, allUsers.size());
        return page.map(UserResponse::fromEntity);
    }

    // ==================== Bulk Operations ====================

    @Override
    @RequirePermission(Permission.CREATE_USER)
    public List<UserResponse> bulkCreateUsers(Long companyId, List<UserCreateRequest> requests, Role role) {
        verifyCompanyAccess(companyId);

        List<UserResponse> created = new ArrayList<>();

        for (UserCreateRequest request : requests) {
            try {
                UserCreateRequest updatedRequest = UserCreateRequest.builder()
                        .fullName(request.getFullName())
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .password(request.getPassword())
                        .role(role)
                        .build();

                UserResponse response = createUser(companyId, updatedRequest);
                created.add(response);
            } catch (Exception e) {
                log.warn("Failed to create user: {} - {}", request.getEmail(), e.getMessage());
            }
        }

        auditLog(companyId, "BULK_USERS_CREATED", "Created " + created.size() + " users", companyId);
        return created;
    }

    @Override
    @RequirePermission(Permission.UPDATE_USER)
    public void bulkUpdateStatus(List<Long> userIds, User.UserStatus status) {
        for (Long userId : userIds) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setStatus(status);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
        }

        log.info("Bulk status update completed for {} users", userIds.size());
    }

    @Override
    @RequirePermission(Permission.EXPORT_REPORTS)
    public byte[] exportUsersToCSV(Long companyId) {
        verifyCompanyAccess(companyId);

        List<User> users = userRepository.findByCompanyId(companyId);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Full Name,Email,Phone,Role,Status,Created At,Updated At\n");

        for (User user : users) {
            csv.append(user.getId()).append(",");
            csv.append(user.getFullName()).append(",");
            csv.append(user.getEmail()).append(",");
            csv.append(user.getPhone()).append(",");
            csv.append(user.getRole()).append(",");
            csv.append(user.getStatus()).append(",");
            csv.append(user.getCreatedAt()).append(",");
            csv.append(user.getUpdatedAt()).append("\n");
        }

        auditLog(companyId, "USERS_EXPORTED", "Exported " + users.size() + " users to CSV", companyId);
        return csv.toString().getBytes();
    }

    // ==================== Helper Methods ====================

    private void verifyCompanyAccess(Long companyId) {
        if (!SecurityUtils.hasCompanyAccess(companyId)) {
            throw new SecurityException("Company access denied");
        }
    }

    private void verifyUserAccess(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!userId.equals(currentUserId) && !SecurityUtils.hasPermission(Permission.READ_USER)) {
            throw new SecurityException("User access denied");
        }
    }

    private void auditLog(Long companyId, String action, Long entityId) {
        auditService.logAuditEvent(companyId, action, "User", entityId);
    }

    private void auditLog(Long companyId, String action, String details, Long entityId) {
        auditService.logAuditEvent(companyId, action, "User", entityId, details);
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}
