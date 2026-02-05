package com.devcast.fleetmanagement.features.payroll.repository;

import com.devcast.fleetmanagement.features.payroll.model.PayrollPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> {
    
    Optional<PayrollPeriod> findByCompanyIdAndStatus(Long companyId, PayrollPeriod.PayrollStatus status);
    
    List<PayrollPeriod> findByCompanyIdAndStatus(Long companyId, PayrollPeriod.PayrollStatus status);
    
    Page<PayrollPeriod> findByCompanyId(Long companyId, Pageable pageable);
    
    Page<PayrollPeriod> findByCompanyIdAndStatus(Long companyId, PayrollPeriod.PayrollStatus status, Pageable pageable);
    
    List<PayrollPeriod> findByCompanyIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            Long companyId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT pp FROM PayrollPeriod pp WHERE pp.company.id = :companyId AND pp.status = 'OPEN' LIMIT 1")
    Optional<PayrollPeriod> getActivePayrollPeriod(@Param("companyId") Long companyId);
    
    @Query("SELECT pp FROM PayrollPeriod pp WHERE pp.company.id = :companyId ORDER BY pp.endDate DESC")
    List<PayrollPeriod> findLatestPeriods(@Param("companyId") Long companyId, Pageable pageable);
}
