package com.devcast.fleetmanagement.features.audit.service;

import com.devcast.fleetmanagement.features.audit.model.AuditLog;
import com.devcast.fleetmanagement.features.audit.repository.AuditLogRepository;
import com.devcast.fleetmanagement.features.company.model.Company;
import com.devcast.fleetmanagement.features.company.repository.CompanyRepository;
import com.devcast.fleetmanagement.features.user.model.User;
import com.devcast.fleetmanagement.features.user.repository.UserRepository;
import com.devcast.fleetmanagement.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit Service
 *
 * Handles centralized audit logging for the application.
 * All audit operations are logged to track compliance, security, and user actions.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    /**
     * Log an audit event
     *
     * @param companyId Company ID for multi-tenant isolation
     * @param action Action performed (e.g., USER_CREATED, USER_UPDATED)
     * @param entity Entity type (e.g., "User", "Company", "Vehicle")
     * @param entityId ID of the entity affected
     */
    public void logAuditEvent(Long companyId, String action, String entity, Long entityId) {
        logAuditEvent(companyId, action, entity, entityId, null);
    }

    /**
     * Log an audit event with additional details
     *
     * @param companyId Company ID for multi-tenant isolation
     * @param action Action performed
     * @param entity Entity type affected
     * @param entityId ID of the entity affected
     * @param details Additional details about the action
     */
    public void logAuditEvent(Long companyId, String action, String entity, Long entityId, String details) {
        try {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found"));

            User currentUser = userRepository.findById(SecurityUtils.getCurrentUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

            AuditLog auditLog = AuditLog.builder()
                    .company(company)
                    .user(currentUser)
                    .action(action)
                    .entity(entity)
                    .entityId(entityId)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

            log.debug("Audit logged - Action: {}, Entity: {}, EntityId: {}, Company: {}",
                    action, entity, entityId, companyId);
        } catch (Exception e) {
            log.error("Failed to log audit event: {}", e.getMessage(), e);
        }
    }

    /**
     * Get audit logs for a specific company
     *
     * @param companyId Company ID
     * @return List of audit logs for the company
     */
    public List<AuditLog> getCompanyAuditLogs(Long companyId) {
        return auditLogRepository.findByCompanyId(companyId);
    }

    /**
     * Get audit logs for a specific user
     *
     * @param userId User ID
     * @return List of audit logs for the user
     */
    public List<AuditLog> getUserAuditLogs(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }

    /**
     * Get audit logs within a time range
     *
     * @param startTime Start time
     * @param endTime End time
     * @return List of audit logs within the time range
     */
    public List<AuditLog> getAuditLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findByTimestampBetween(startTime, endTime);
    }
}
