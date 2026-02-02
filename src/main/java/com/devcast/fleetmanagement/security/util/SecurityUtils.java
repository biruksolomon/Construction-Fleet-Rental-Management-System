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
     */
    public static JwtAuthenticationFilter.JwtUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationFilter.JwtUserDetails) {
            return (JwtAuthenticationFilter.JwtUserDetails) authentication.getDetails();
        }
        throw new IllegalStateException("User not authenticated or JWT details not found");
    }

    /**
     * Get current user's ID
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    /**
     * Get current user's company ID
     */
    public static Long getCurrentCompanyId() {
        return getCurrentUser().getCompanyId();
    }

    /**
     * Get current user's email
     */
    public static String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    /**
     * Get current user's role
     */
    public static Role getCurrentUserRole() {
        String roleString = getCurrentUser().getRole();
        return Role.valueOf(roleString.replace("ROLE_", ""));
    }

    /**
     * Check if current user has specific permission
     */
    public static boolean hasPermission(Permission permission) {
        Role role = getCurrentUserRole();
        return RolePermissionMap.hasPermission(role, permission);
    }

    /**
     * Check if current user has any of the specified permissions
     */
    public static boolean hasAnyPermission(Permission... permissions) {
        for (Permission permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user has all of the specified permissions
     */
    public static boolean hasAllPermissions(Permission... permissions) {
        for (Permission permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if current user has specific role
     */
    public static boolean hasRole(Role role) {
        return getCurrentUserRole().equals(role);
    }

    /**
     * Check if current user has any of the specified roles
     */
    public static boolean hasAnyRole(Role... roles) {
        Role currentRole = getCurrentUserRole();
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
     */
    public static boolean canAccessCompany(Long companyId) {
        return getCurrentCompanyId().equals(companyId);
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
