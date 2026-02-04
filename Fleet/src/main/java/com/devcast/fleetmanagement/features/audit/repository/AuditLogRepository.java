package com.devcast.fleetmanagement.features.audit.repository;


import com.devcast.fleetmanagement.features.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByCompanyId(Long companyId);
    List<AuditLog> findByUserId(Long userId);
    List<AuditLog> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
}
