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
        // User Management
        ENDPOINT_PERMISSION_MAP.put("POST:/api/users", Permission.CREATE_USER);
        ENDPOINT_PERMISSION_MAP.put("GET:/api/users", Permission.READ_USER);
        ENDPOINT_PERMISSION_MAP.put("PUT:/api/users/.*", Permission.UPDATE_USER);
        ENDPOINT_PERMISSION_MAP.put("DELETE:/api/users/.*", Permission.DELETE_USER);

        // Vehicle Management
        ENDPOINT_PERMISSION_MAP.put("POST:/api/vehicles", Permission.CREATE_VEHICLE);
        ENDPOINT_PERMISSION_MAP.put("GET:/api/vehicles", Permission.READ_VEHICLE);
        ENDPOINT_PERMISSION_MAP.put("PUT:/api/vehicles/.*", Permission.UPDATE_VEHICLE);
        ENDPOINT_PERMISSION_MAP.put("DELETE:/api/vehicles/.*", Permission.DELETE_VEHICLE);

        // Driver Management
        ENDPOINT_PERMISSION_MAP.put("POST:/api/drivers", Permission.CREATE_DRIVER);
        ENDPOINT_PERMISSION_MAP.put("GET:/api/drivers", Permission.READ_DRIVER);
        ENDPOINT_PERMISSION_MAP.put("PUT:/api/drivers/.*", Permission.UPDATE_DRIVER);
        ENDPOINT_PERMISSION_MAP.put("DELETE:/api/drivers/.*", Permission.DELETE_DRIVER);

        // Rental Management
        ENDPOINT_PERMISSION_MAP.put("POST:/api/rentals", Permission.CREATE_RENTAL);
        ENDPOINT_PERMISSION_MAP.put("GET:/api/rentals", Permission.READ_RENTAL);
        ENDPOINT_PERMISSION_MAP.put("PUT:/api/rentals/.*", Permission.UPDATE_RENTAL);
        ENDPOINT_PERMISSION_MAP.put("DELETE:/api/rentals/.*", Permission.DELETE_RENTAL);

        // Invoicing
        ENDPOINT_PERMISSION_MAP.put("POST:/api/invoices", Permission.CREATE_INVOICE);
        ENDPOINT_PERMISSION_MAP.put("GET:/api/invoices", Permission.READ_INVOICE);
        ENDPOINT_PERMISSION_MAP.put("PUT:/api/invoices/.*", Permission.UPDATE_INVOICE);
        ENDPOINT_PERMISSION_MAP.put("DELETE:/api/invoices/.*", Permission.DELETE_INVOICE);

        // Payroll
        ENDPOINT_PERMISSION_MAP.put("POST:/api/payroll", Permission.CREATE_PAYROLL);
        ENDPOINT_PERMISSION_MAP.put("GET:/api/payroll", Permission.READ_PAYROLL);
        ENDPOINT_PERMISSION_MAP.put("PUT:/api/payroll/.*", Permission.UPDATE_PAYROLL);
        ENDPOINT_PERMISSION_MAP.put("DELETE:/api/payroll/.*", Permission.DELETE_PAYROLL);
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
                Role userRole = Role.valueOf(roleString.replace("ROLE_", ""));

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
        // Skip filtering for public endpoints
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") || path.startsWith("/api/public/") || path.startsWith("/api/swagger-ui/index.html");
    }
}
