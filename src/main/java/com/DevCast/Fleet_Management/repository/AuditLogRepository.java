package com.DevCast.Fleet_Management.repository;


import com.DevCast.Fleet_Management.model.AuditLog;
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
