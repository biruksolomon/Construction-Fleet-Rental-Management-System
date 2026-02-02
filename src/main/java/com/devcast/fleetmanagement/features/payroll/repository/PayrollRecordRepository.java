package com.devcast.fleetmanagement.features.payroll.repository;

import com.devcast.fleetmanagement.features.payroll.model.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {
    List<PayrollRecord> findByPayrollPeriodId(Long payrollPeriodId);
    List<PayrollRecord> findByDriverId(Long driverId);
}
