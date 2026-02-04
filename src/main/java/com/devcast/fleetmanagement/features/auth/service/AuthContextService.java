package com.devcast.fleetmanagement.features.auth.service;

import com.devcast.fleetmanagement.features.auth.dto.AuthContextResponse;
import com.devcast.fleetmanagement.features.company.model.Company;
import com.devcast.fleetmanagement.features.company.repository.CompanyRepository;
import com.devcast.fleetmanagement.features.user.model.User;
import com.devcast.fleetmanagement.features.user.model.util.Permission;
import com.devcast.fleetmanagement.features.user.model.util.Role;
import com.devcast.fleetmanagement.features.user.model.util.RolePermissionMap;
import com.devcast.fleetmanagement.features.user.repository.UserRepository;
import com.devcast.fleetmanagement.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Auth Context Service
 *
 * Responsible for building the complete auth context for a user.
 * This is the single source of truth for frontend auth state.
 *
 * Frontend calls GET /api/auth/context to initialize UI state.
 * All user permissions, roles, and company features are available here.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthContextService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Build complete auth context for a user from JWT token
     *
     * @param token JWT access token
     * @return AuthContextResponse with user, company, roles, permissions, and features
     * @throws RuntimeException if user or company not found
     */
    public AuthContextResponse buildAuthContext(String token) {
        // Extract claims from JWT
        Long userId = jwtTokenProvider.getUserIdFromJwt(token);
        Long companyId = jwtTokenProvider.getCompanyIdFromJwt(token);
        String email = jwtTokenProvider.getEmailFromJwt(token);
        List<String> rolesFromToken = jwtTokenProvider.getRolesFromJwt(token);
        List<String> permissionsFromToken = jwtTokenProvider.getPermissionsFromJwt(token);

        // Fetch fresh user and company data
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        // Validate company consistency
        if (!user.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("User does not belong to company: " + companyId);
        }

        // Build user context
        AuthContextResponse.UserContext userContext = AuthContextResponse.UserContext.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();

        // Build company context
        AuthContextResponse.CompanyContext companyContext = AuthContextResponse.CompanyContext.builder()
                .id(company.getId())
                .name(company.getName())
                .status(company.getStatus().name())
                .businessType(company.getBusinessType().name())
                .currency(company.getCurrency())
                .timezone(company.getTimezone())
                .language(company.getLanguage())
                .build();

        // Get permissions for the user's role
        Set<Permission> userPermissions = RolePermissionMap.getPermissionsForRole(user.getRole());
        List<String> permissionCodes = userPermissions.stream()
                .map(Permission::getCode)
                .sorted()
                .collect(Collectors.toList());

        // Build features map based on company subscription and user role
        Map<String, Boolean> features = buildFeatures(company, user.getRole());

        // Build the full auth context response
        return AuthContextResponse.builder()
                .user(userContext)
                .company(companyContext)
                .roles(rolesFromToken != null ? rolesFromToken : Collections.singletonList(user.getRole().getAuthority()))
                .permissions(permissionCodes)
                .features(features)
                .build();
    }

    /**
     * Build features availability map based on company and user role
     * Features determine what functionality is available to the user
     *
     * @param company The user's company
     * @param role The user's role
     * @return Map of feature names to availability (true/false)
     */
    private Map<String, Boolean> buildFeatures(Company company, Role role) {
        Map<String, Boolean> features = new HashMap<>();

        // All companies have these core features
        features.put("vehicleManagement", true);
        features.put("driverManagement", true);
        features.put("clients", true);

        // Feature access by role
        features.put("gpsTracking", RolePermissionMap.hasPermission(role, Permission.VIEW_GPS_DATA));
        features.put("fuelAnalysis", RolePermissionMap.hasPermission(role, Permission.VIEW_FUEL_ANALYSIS));
        features.put("maintenance", RolePermissionMap.hasPermission(role, Permission.CREATE_MAINTENANCE));
        features.put("invoicing", RolePermissionMap.hasPermission(role, Permission.CREATE_INVOICE));
        features.put("payroll", RolePermissionMap.hasPermission(role, Permission.CREATE_PAYROLL));
        features.put("rentalContracts", RolePermissionMap.hasPermission(role, Permission.CREATE_RENTAL));
        features.put("auditLogs", RolePermissionMap.hasPermission(role, Permission.VIEW_AUDIT_LOG));
        features.put("companySettings", RolePermissionMap.hasPermission(role, Permission.MANAGE_COMPANY_SETTINGS));
        features.put("userManagement", RolePermissionMap.hasPermission(role, Permission.MANAGE_USER_ROLES));

        return features;
    }

    /**
     * Get user's permissions as a list of permission codes
     *
     * @param userId User ID
     * @return List of permission codes
     */
    public List<String> getUserPermissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Set<Permission> permissions = RolePermissionMap.getPermissionsForRole(user.getRole());
        return permissions.stream()
                .map(Permission::getCode)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Check if user has a specific permission
     *
     * @param userId User ID
     * @param permissionCode Permission code to check
     * @return true if user has permission, false otherwise
     */
    public boolean hasPermission(Long userId, String permissionCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        try {
            Permission permission = Permission.valueOf(permissionCode);
            return RolePermissionMap.hasPermission(user.getRole(), permission);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid permission code: {}", permissionCode);
            return false;
        }
    }
}
