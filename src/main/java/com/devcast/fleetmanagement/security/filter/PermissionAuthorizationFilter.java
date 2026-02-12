package com.devcast.fleetmanagement.security.filter;

import com.devcast.fleetmanagement.features.user.model.util.Permission;
import com.devcast.fleetmanagement.features.user.model.util.Role;
import com.devcast.fleetmanagement.features.user.model.util.RolePermissionMap;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Permission-Based Authorization Filter
 * Validates user permissions for sensitive operations
 * Can be extended to check endpoint-level permissions
 */
public class PermissionAuthorizationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(PermissionAuthorizationFilter.class);

    /**
     * Map endpoint patterns to required permissions
     * This can be externalized to configuration
     */
    private static final Map<String, Permission> ENDPOINT_PERMISSION_MAP = new HashMap<>();

    static {
        // User Management (path: /users)
        ENDPOINT_PERMISSION_MAP.put("POST:/users", Permission.CREATE_USER);
        ENDPOINT_PERMISSION_MAP.put("GET:/users", Permission.READ_USER);
        ENDPOINT_PERMISSION_MAP.put("PUT:/users/.*", Permission.UPDATE_USER);
        ENDPOINT_PERMISSION_MAP.put("DELETE:/users/.*", Permission.DELETE_USER);

        // Vehicle Management (path: /v1/vehicles)
        ENDPOINT_PERMISSION_MAP.put("POST:/v1/vehicles", Permission.CREATE_VEHICLE);
        ENDPOINT_PERMISSION_MAP.put("GET:/v1/vehicles", Permission.READ_VEHICLE);
        ENDPOINT_PERMISSION_MAP.put("PUT:/v1/vehicles/.*", Permission.UPDATE_VEHICLE);
        ENDPOINT_PERMISSION_MAP.put("DELETE:/v1/vehicles/.*", Permission.DELETE_VEHICLE);

        // Driver Management (path: /drivers)
        ENDPOINT_PERMISSION_MAP.put("POST:/drivers", Permission.CREATE_DRIVER);
        ENDPOINT_PERMISSION_MAP.put("GET:/drivers", Permission.READ_DRIVER);
        ENDPOINT_PERMISSION_MAP.put("PUT:/drivers/.*", Permission.UPDATE_DRIVER);
        ENDPOINT_PERMISSION_MAP.put("DELETE:/drivers/.*", Permission.DELETE_DRIVER);

        // Rental Management (path: /v1/rentals)
        ENDPOINT_PERMISSION_MAP.put("POST:/v1/rentals", Permission.CREATE_RENTAL);
        ENDPOINT_PERMISSION_MAP.put("GET:/v1/rentals", Permission.READ_RENTAL);
        ENDPOINT_PERMISSION_MAP.put("PUT:/v1/rentals/.*", Permission.UPDATE_RENTAL);
        ENDPOINT_PERMISSION_MAP.put("DELETE:/v1/rentals/.*", Permission.DELETE_RENTAL);

        // Invoicing (path: /invoices)
        ENDPOINT_PERMISSION_MAP.put("POST:/invoices", Permission.CREATE_INVOICE);
        ENDPOINT_PERMISSION_MAP.put("GET:/invoices", Permission.READ_INVOICE);
        ENDPOINT_PERMISSION_MAP.put("PUT:/invoices/.*", Permission.UPDATE_INVOICE);
        ENDPOINT_PERMISSION_MAP.put("DELETE:/invoices/.*", Permission.DELETE_INVOICE);

        // Payroll (path: /payroll)
        ENDPOINT_PERMISSION_MAP.put("POST:/payroll", Permission.CREATE_PAYROLL);
        ENDPOINT_PERMISSION_MAP.put("GET:/payroll", Permission.READ_PAYROLL);
        ENDPOINT_PERMISSION_MAP.put("PUT:/payroll/.*", Permission.UPDATE_PAYROLL);
        ENDPOINT_PERMISSION_MAP.put("DELETE:/payroll/.*", Permission.DELETE_PAYROLL);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String requestMethod = request.getMethod();
            String requestPath = request.getRequestURI();
            String endpointKey = requestMethod + ":" + requestPath;

            Permission requiredPermission = findRequiredPermission(endpointKey);

            if (requiredPermission != null) {
                String roleString = authentication.getAuthorities().iterator().next().getAuthority();
                // Strip ROLE_ prefix to get the enum name (e.g. "ROLE_OWNER" -> "OWNER")
                String roleName = roleString.startsWith("ROLE_") ? roleString.substring(5) : roleString;
                Role userRole = Role.valueOf(roleName);

                if (!RolePermissionMap.hasPermission(userRole, requiredPermission)) {
                    logger.warn("Access denied for user with role: {} for permission: {}",
                            userRole, requiredPermission.getCode());
                    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "Access denied: Insufficient permissions");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Find required permission for an endpoint pattern
     */
    private Permission findRequiredPermission(String endpointKey) {
        for (Map.Entry<String, Permission> entry : ENDPOINT_PERMISSION_MAP.entrySet()) {
            if (endpointKey.matches(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Skip filtering for public/auth endpoints, swagger, and health
        String path = request.getRequestURI();
        return path.startsWith("/auth/")
                || path.startsWith("/public/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars/")
                || path.equals("/health")
                || path.equals("/info");
    }
}
