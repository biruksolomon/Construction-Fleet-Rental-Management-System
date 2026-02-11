package com.devcast.fleetmanagement.security.util;



import com.devcast.fleetmanagement.features.user.model.util.Permission;
import com.devcast.fleetmanagement.features.user.model.util.Role;
import com.devcast.fleetmanagement.features.user.model.util.RolePermissionMap;
import com.devcast.fleetmanagement.security.filter.JwtAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Security Utility class for accessing security context information
 * Provides helper methods for permission and role checking
 */
@Component
public class SecurityUtils {

    /**
     * Get current authenticated user details
     * Returns null if user is not authenticated
     */
    public static JwtAuthenticationFilter.JwtUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationFilter.JwtUserDetails) {
            return (JwtAuthenticationFilter.JwtUserDetails) authentication.getDetails();
        }
        return null;
    }

    /**
     * Get current user's ID, returns null if not authenticated
     */
    public static Long getCurrentUserId() {
        JwtAuthenticationFilter.JwtUserDetails user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * Get current user's company ID, returns null if not authenticated
     */
    public static Long getCurrentCompanyId() {
        JwtAuthenticationFilter.JwtUserDetails user = getCurrentUser();
        return user != null ? user.getCompanyId() : null;
    }

    /**
     * Get current user's email, returns null if not authenticated
     */
    public static String getCurrentUserEmail() {
        JwtAuthenticationFilter.JwtUserDetails user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    /**
     * Get current user's role, returns null if not authenticated
     */
    public static Role getCurrentUserRole() {
        JwtAuthenticationFilter.JwtUserDetails user = getCurrentUser();
        if (user == null) {
            return null;
        }
        String roleString = user.getRole();
        return Role.valueOf(roleString.replace("ROLE_", ""));
    }

    /**
     * Check if current user has specific permission
     * Returns false if user is not authenticated
     */
    public static boolean hasPermission(Permission permission) {
        Role role = getCurrentUserRole();
        if (role == null) {
            return false;
        }
        return RolePermissionMap.hasPermission(role, permission);
    }

    /**
     * Check if current user has any of the specified permissions
     * Returns false if user is not authenticated
     */
    public static boolean hasAnyPermission(Permission... permissions) {
        if (!isAuthenticated()) {
            return false;
        }
        for (Permission permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user has all of the specified permissions
     * Returns false if user is not authenticated
     */
    public static boolean hasAllPermissions(Permission... permissions) {
        if (!isAuthenticated()) {
            return false;
        }
        for (Permission permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if current user has specific role
     * Returns false if user is not authenticated
     */
    public static boolean hasRole(Role role) {
        Role currentRole = getCurrentUserRole();
        return currentRole != null && currentRole.equals(role);
    }

    /**
     * Check if current user has any of the specified roles
     * Returns false if user is not authenticated
     */
    public static boolean hasAnyRole(Role... roles) {
        Role currentRole = getCurrentUserRole();
        if (currentRole == null) {
            return false;
        }
        for (Role role : roles) {
            if (role.equals(currentRole)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user is owner or admin
     */
    public static boolean isOwnerOrAdmin() {
        return hasAnyRole(Role.OWNER, Role.ADMIN);
    }

    /**
     * Check if current user is fleet manager or above
     */
    public static boolean isFleetManagerOrAbove() {
        return hasAnyRole(Role.OWNER, Role.ADMIN, Role.FLEET_MANAGER);
    }

    /**
     * Check if current user is accountant or above
     */
    public static boolean isAccountantOrAbove() {
        return hasAnyRole(Role.OWNER, Role.ADMIN, Role.ACCOUNTANT);
    }

    /**
     * Verify user has access to specific company (multi-tenant check)
     * Returns false if user is not authenticated
     */
    public static boolean canAccessCompany(Long companyId) {
        Long userCompanyId = getCurrentCompanyId();
        return userCompanyId != null && userCompanyId.equals(companyId);
    }

    /**
     * Verify user has access to specific company (alias for canAccessCompany)
     * Returns false if user is not authenticated
     */
    public static boolean hasCompanyAccess(Long companyId) {
        return canAccessCompany(companyId);
    }

    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Get all permissions for current user
     */
    public static java.util.Set<Permission> getCurrentUserPermissions() {
        return RolePermissionMap.getPermissionsForRole(getCurrentUserRole());
    }

    /**
     * Get current user's role display name
     */
    public static String getCurrentUserRoleDisplayName() {
        return getCurrentUserRole().getDisplayName();
    }
}
