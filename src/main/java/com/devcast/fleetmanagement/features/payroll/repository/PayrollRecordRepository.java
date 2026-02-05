package com.devcast.fleetmanagement.features.payroll.repository;

import com.devcast.fleetmanagement.features.payroll.model.PayrollRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {
    
    List<PayrollRecord> findByPayrollPeriodId(Long payrollPeriodId);
    
    Page<PayrollRecord> findByPayrollPeriodId(Long payrollPeriodId, Pageable pageable);
    
    List<PayrollRecord> findByDriverId(Long driverId);
    
    List<PayrollRecord> findByDriverIdAndStatus(Long driverId, PayrollRecord.PayrollStatus status);
    
    List<PayrollRecord> findByPayrollPeriodIdAndStatus(Long periodId, PayrollRecord.PayrollStatus status);
    
    Page<PayrollRecord> findByPayrollPeriodIdAndStatus(Long periodId, PayrollRecord.PayrollStatus status, Pageable pageable);
    
    @Query("SELECT pr FROM PayrollRecord pr WHERE pr.payrollPeriod.id = :periodId AND pr.driver.company.id = :companyId")
    List<PayrollRecord> findPayrollRecordsByPeriodAndCompany(@Param("companyId") Long companyId, @Param("periodId") Long periodId);
    
    @Query("SELECT SUM(pr.netPay) FROM PayrollRecord pr WHERE pr.payrollPeriod.id = :periodId")
    Optional<BigDecimal> calculateTotalNetPayForPeriod(@Param("periodId") Long periodId);
    
    @Query("SELECT SUM(pr.grossSalary) FROM PayrollRecord pr WHERE pr.payrollPeriod.id = :periodId")
    Optional<BigDecimal> calculateTotalGrossPayForPeriod(@Param("periodId") Long periodId);
    
    @Query("SELECT COUNT(pr) FROM PayrollRecord pr WHERE pr.payrollPeriod.id = :periodId AND pr.status = 'APPROVED'")
    long countApprovedRecordsForPeriod(@Param("periodId") Long periodId);


    @Query("""
    SELECT pr FROM PayrollRecord pr
    WHERE pr.driver.id = :driverId
    ORDER BY pr.payrollPeriod.endDate DESC
""")
    Page<PayrollRecord> getEmployeePayrollHistory(
            @Param("driverId") Long driverId,
            Pageable pageable
    );

}
