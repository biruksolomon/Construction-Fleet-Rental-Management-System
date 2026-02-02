package com.devcast.fleetmanagement.features.audit.service;

import com.devcast.fleetmanagement.features.audit.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * Audit Log Service Interface
 * Handles audit trail logging, compliance tracking, and security monitoring
 */
public interface AuditLogService {

    // ==================== Audit Log Operations ====================

    /**
     * Log audit event
     */
    AuditLog logAuditEvent(Long userId, String action, String entityType, Long entityId, String details);

    /**
     * Get audit logs for entity
     */
    Page<AuditLog> getAuditLogsForEntity(String entityType, Long entityId, Pageable pageable);

    /**
     * Get audit logs for user
     */
    Page<AuditLog> getAuditLogsForUser(Long userId, Pageable pageable);

    /**
     * Get all audit logs in company
     */
    Page<AuditLog> getAuditLogsByCompany(Long companyId, Pageable pageable);

    /**
     * Get audit logs by date range
     */
    List<AuditLog> getAuditLogsByDateRange(Long companyId, Long fromDate, Long toDate);

    /**
     * Get audit logs by action type
     */
    Page<AuditLog> getAuditLogsByAction(Long companyId, String action, Pageable pageable);

    /**
     * Get specific audit log
     */
    Optional<AuditLog> getAuditLog(Long logId);

    // ==================== Security Monitoring ====================

    /**
     * Get user activity logs
     */
    List<UserActivity> getUserActivityLogs(Long userId, Long fromDate, Long toDate);

    /**
     * Get suspicious activities
     */
    List<SuspiciousActivity> getSuspiciousActivities(Long companyId);

    /**
     * Get failed login attempts
     */
    List<FailedLoginAttempt> getFailedLoginAttempts(Long companyId, Long fromDate, Long toDate);

    /**
     * Get unauthorized access attempts
     */
    List<UnauthorizedAccess> getUnauthorizedAccessAttempts(Long companyId);

    /**
     * Alert on suspicious pattern
     */
    void alertSuspiciousPattern(Long userId, String pattern);

    // ==================== Data Change Tracking ====================

    /**
     * Get entity change history
     */
    List<EntityChangeRecord> getEntityChangeHistory(String entityType, Long entityId);

    /**
     * Get who changed what
     */
    List<DataChangeTrail> getDataChangeTrail(String entityType, Long entityId, Long fromDate, Long toDate);

    /**
     * Restore previous version
     */
    void restorePreviousVersion(String entityType, Long entityId, Long version);

    /**
     * Get deleted records log
     */
    List<DeletedRecord> getDeletedRecords(Long companyId, Long fromDate, Long toDate);

    // ==================== Compliance & Reporting ====================

    /**
     * Generate audit trail report
     */
    String generateAuditTrailReport(Long companyId, Long fromDate, Long toDate);

    /**
     * Generate compliance report
     */
    String generateComplianceReport(Long companyId, Long fromDate, Long toDate);

    /**
     * Generate security audit report
     */
    String generateSecurityAuditReport(Long companyId);

    /**
     * Export audit logs to CSV
     */
    byte[] exportAuditLogsToCSV(Long companyId, Long fromDate, Long toDate);

    /**
     * Verify data integrity
     */
    DataIntegrityReport verifyDataIntegrity(Long companyId);

    // ==================== Retention & Cleanup ====================

    /**
     * Archive old audit logs
     */
    void archiveOldAuditLogs(int olderThanDays);

    /**
     * Delete audit logs (with approval)
     */
    void deleteAuditLogs(Long companyId, Long fromDate, Long toDate, String reason);

    /**
     * Get audit log retention policy
     */
    RetentionPolicy getRetentionPolicy(Long companyId);

    /**
     * Set retention policy
     */
    void setRetentionPolicy(Long companyId, RetentionPolicy policy);

    // Data Transfer Objects

    record UserActivity(
            Long userId,
            String userName,
            String action,
            String entityType,
            Long entityId,
            Long timestamp,
            String ipAddress,
            String userAgent
    ) {}

    record SuspiciousActivity(
            Long activityId,
            Long userId,
            String activityType,
            String description,
            String severity,
            Long timestamp,
            String recommendation
    ) {}

    record FailedLoginAttempt(
            Long attemptId,
            String email,
            Long timestamp,
            String ipAddress,
            String reason
    ) {}

    record UnauthorizedAccess(
            Long accessId,
            Long userId,
            String attemptedResource,
            Long timestamp,
            String ipAddress,
            String permission
    ) {}

    record EntityChangeRecord(
            Long changeId,
            String entityType,
            Long entityId,
            Long userId,
            String action,
            String oldValue,
            String newValue,
            Long timestamp
    ) {}

    record DataChangeTrail(
            Long changeId,
            Long userId,
            String fieldName,
            String oldValue,
            String newValue,
            Long timestamp
    ) {}

    record DeletedRecord(
            Long recordId,
            String entityType,
            Long entityId,
            Long userId,
            Long deletionDate,
            String reason
    ) {}

    record DataIntegrityReport(
            Long companyId,
            boolean isIntegrityValid,
            List<String> anomalies,
            List<String> recommendations,
            Long reportDate
    ) {}

    record RetentionPolicy(
            int retentionDays,
            boolean autoArchive,
            boolean autoDelete,
            String archiveLocation,
            String deletionApprovalRequired
    ) {}
}
