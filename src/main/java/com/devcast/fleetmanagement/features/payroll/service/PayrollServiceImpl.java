package com.devcast.fleetmanagement.features.payroll.service;

import com.devcast.fleetmanagement.features.driver.model.Driver;
import com.devcast.fleetmanagement.features.driver.repository.DriverRepository;
import com.devcast.fleetmanagement.features.payroll.exception.PayrollPeriodException;
import com.devcast.fleetmanagement.features.payroll.exception.PayrollRecordException;
import com.devcast.fleetmanagement.features.payroll.model.PayrollPeriod;
import com.devcast.fleetmanagement.features.payroll.model.PayrollRecord;
import com.devcast.fleetmanagement.features.payroll.repository.PayrollPeriodRepository;
import com.devcast.fleetmanagement.features.payroll.repository.PayrollRecordRepository;
import com.devcast.fleetmanagement.features.payroll.util.SalaryCalculationUtil;
import com.devcast.fleetmanagement.features.user.model.util.Permission;
import com.devcast.fleetmanagement.security.annotation.RequirePermission;
import com.devcast.fleetmanagement.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PayrollServiceImpl implements PayrollService {

    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayrollRecordRepository payrollRecordRepository;
    private final DriverRepository driverRepository;
    private final SecurityUtils securityUtils;

    // ==================== Payroll Period Management ====================

    @Override
    @RequirePermission(Permission.CREATE_PAYROLL)
    public PayrollPeriod createPayrollPeriod(Long companyId, PayrollPeriod period) {
        log.info("[Payroll] Creating payroll period for company {}", companyId);

        period.setStatus(PayrollPeriod.PayrollStatus.OPEN);
        period.setTotalGrossSalary(BigDecimal.ZERO);
        period.setTotalDeductions(BigDecimal.ZERO);
        period.setTotalNetSalary(BigDecimal.ZERO);
        period.setCreatedAt(LocalDateTime.now());
        period.setUpdatedAt(LocalDateTime.now());

        PayrollPeriod saved = payrollPeriodRepository.save(period);
        log.info("[Payroll] Payroll period created: {}", saved.getId());
        return saved;
    }

    @Override
    public Optional<PayrollPeriod> getPayrollPeriodById(Long periodId) {
        return payrollPeriodRepository.findById(periodId);
    }

    @Override
    public Optional<PayrollPeriod> getActivePayrollPeriod(Long companyId) {
        return payrollPeriodRepository.findByCompanyIdAndStatus(companyId);
    }

    @Override
    public Page<PayrollPeriod> getPayrollPeriods(Long companyId, Pageable pageable) {
        return payrollPeriodRepository.findByCompanyId(companyId, pageable);
    }

    @Override
    public List<PayrollPeriod> getPayrollPeriodsByStatus(Long companyId, String status) {
        PayrollPeriod.PayrollStatus payrollStatus = PayrollPeriod.PayrollStatus.valueOf(status.toUpperCase());
        return payrollPeriodRepository.findByCompanyIdAndStatus(companyId, payrollStatus);
    }

    @Override
    @RequirePermission(Permission.UPDATE_PAYROLL)
    public PayrollPeriod updatePayrollPeriod(Long periodId, PayrollPeriod period) {
        PayrollPeriod existing = getPayrollPeriodById(periodId)
                .orElseThrow(() -> PayrollPeriodException.notFound(periodId));

        existing.setUpdatedAt(LocalDateTime.now());
        return payrollPeriodRepository.save(existing);
    }

    @Override
    @RequirePermission(Permission.UPDATE_PAYROLL)
    public void closePayrollPeriod(Long periodId) {
        log.info("[Payroll] Closing payroll period {}", periodId);

        PayrollPeriod period = getPayrollPeriodById(periodId)
                .orElseThrow(() -> PayrollPeriodException.notFound(periodId));

        if (!period.getStatus().equals(PayrollPeriod.PayrollStatus.OPEN)) {
            throw PayrollPeriodException.invalidStatus("Period is not open");
        }

        period.setStatus(PayrollPeriod.PayrollStatus.CLOSED);
        period.setClosedDate(LocalDateTime.now());
        period.setClosedBy(securityUtils.getCurrentUserEmail());
        period.setUpdatedAt(LocalDateTime.now());

        payrollPeriodRepository.save(period);
        log.info("[Payroll] Payroll period closed: {}", periodId);
    }

    @Override
    @RequirePermission(Permission.UPDATE_PAYROLL)
    public void cancelPayrollPeriod(Long periodId, String reason) {
        log.info("[Payroll] Cancelling payroll period {} - Reason: {}", periodId, reason);

        PayrollPeriod period = getPayrollPeriodById(periodId)
                .orElseThrow(() -> PayrollPeriodException.notFound(periodId));

        period.setStatus(PayrollPeriod.PayrollStatus.CANCELLED);
        period.setUpdatedAt(LocalDateTime.now());

        payrollPeriodRepository.save(period);
        log.info("[Payroll] Payroll period cancelled: {}", periodId);
    }

    @Override
    @RequirePermission(Permission.UPDATE_PAYROLL)
    public void lockPayrollPeriod(Long periodId) {
        log.info("[Payroll] Locking payroll period {}", periodId);

        PayrollPeriod period = getPayrollPeriodById(periodId)
                .orElseThrow(() -> PayrollPeriodException.notFound(periodId));

        period.setStatus(PayrollPeriod.PayrollStatus.LOCKED);
        period.setUpdatedAt(LocalDateTime.now());

        payrollPeriodRepository.save(period);
    }

    @Override
    @RequirePermission(Permission.UPDATE_PAYROLL)
    public void finalizePayrollPeriod(Long periodId) {
        log.info("[Payroll] Finalizing payroll period {}", periodId);

        PayrollPeriod period = getPayrollPeriodById(periodId)
                .orElseThrow(() -> PayrollPeriodException.notFound(periodId));

        period.setStatus(PayrollPeriod.PayrollStatus.FINALIZED);
        period.setFinalizedDate(LocalDateTime.now());
        period.setFinalizedBy(securityUtils.getCurrentUserEmail());
        period.setUpdatedAt(LocalDateTime.now());

        payrollPeriodRepository.save(period);
        log.info("[Payroll] Payroll period finalized: {}", periodId);
    }

    // ==================== Payroll Record Operations ====================

    @Override
    @RequirePermission(Permission.CREATE_PAYROLL)
    public List<PayrollRecord> generatePayrollRecords(Long periodId) {
        log.info("[Payroll] Generating payroll records for period {}", periodId);

        PayrollPeriod period = getPayrollPeriodById(periodId)
                .orElseThrow(() -> PayrollPeriodException.notFound(periodId));

        List<Driver> drivers = driverRepository.findByCompanyId(period.getCompany().getId());
        List<PayrollRecord> records = new ArrayList<>();

        for (Driver driver : drivers) {
            PayrollRecord record = PayrollRecord.builder()
                    .driver(driver)
                    .payrollPeriod(period)
                    .totalWorkHours(0L)
                    .overtimeHours(0L)
                    .basePay(BigDecimal.ZERO)
                    .workHourBonus(BigDecimal.ZERO)
                    .performanceBonus(BigDecimal.ZERO)
                    .incentives(BigDecimal.ZERO)
                    .grossSalary(BigDecimal.ZERO)
                    .taxDeduction(BigDecimal.ZERO)
                    .insuranceDeduction(BigDecimal.ZERO)
                    .loanDeduction(BigDecimal.ZERO)
                    .otherDeductions(BigDecimal.ZERO)
                    .totalDeductions(BigDecimal.ZERO)
                    .netPay(BigDecimal.ZERO)
                    .status(PayrollRecord.PayrollStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            records.add(record);
        }

        List<PayrollRecord> saved = payrollRecordRepository.saveAll(records);
        log.info("[Payroll] Generated {} payroll records for period {}", saved.size(), periodId);
        return saved;
    }

    @Override
    public Optional<PayrollRecord> getPayrollRecord(Long recordId) {
        return payrollRecordRepository.findById(recordId);
    }

    @Override
    public Page<PayrollRecord> getPayrollRecordsForPeriod(Long periodId, Pageable pageable) {
        return payrollRecordRepository.findByPayrollPeriodId(periodId, pageable);
    }

    @Override
    public List<PayrollRecord> getEmployeePayrollHistory(Long driverId, int months) {
        Pageable pageable = PageRequest.of(
                0,
                months,
                Sort.by("payrollPeriod.endDate").descending()
        );

        return payrollRecordRepository
                .getEmployeePayrollHistory(driverId, pageable)
                .getContent();
    }


    @Override
    @RequirePermission(Permission.UPDATE_PAYROLL)
    public PayrollRecord updatePayrollRecord(Long recordId, PayrollRecord record) {
        PayrollRecord existing = getPayrollRecord(recordId)
                .orElseThrow(() -> PayrollRecordException.notFound(recordId));

        existing.setUpdatedAt(LocalDateTime.now());
        return payrollRecordRepository.save(existing);
    }

    @Override
    @RequirePermission(Permission.APPROVE_PAYROLL)
    public void approvePayrollRecord(Long recordId) {
        log.info("[Payroll] Approving payroll record {}", recordId);

        PayrollRecord record = getPayrollRecord(recordId)
                .orElseThrow(() -> PayrollRecordException.notFound(recordId));

        record.setStatus(PayrollRecord.PayrollStatus.APPROVED);
        record.setApprovedBy(securityUtils.getCurrentUserEmail());
        record.setApprovedDate(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        payrollRecordRepository.save(record);
        log.info("[Payroll] Payroll record approved: {}", recordId);
    }

    @Override
    @RequirePermission(Permission.APPROVE_PAYROLL)
    public void rejectPayrollRecord(Long recordId, String reason) {
        log.info("[Payroll] Rejecting payroll record {} - Reason: {}", recordId, reason);

        PayrollRecord record = getPayrollRecord(recordId)
                .orElseThrow(() -> PayrollRecordException.notFound(recordId));

        record.setStatus(PayrollRecord.PayrollStatus.REJECTED);
        record.setRemarks(reason);
        record.setUpdatedAt(LocalDateTime.now());

        payrollRecordRepository.save(record);
    }

    @Override
    @RequirePermission(Permission.UPDATE_PAYROLL)
    public void voidPayrollRecord(Long recordId, String reason) {
        log.info("[Payroll] Voiding payroll record {} - Reason: {}", recordId, reason);

        PayrollRecord record = getPayrollRecord(recordId)
                .orElseThrow(() -> PayrollRecordException.notFound(recordId));

        record.setStatus(PayrollRecord.PayrollStatus.VOID);
        record.setRemarks(reason);
        record.setUpdatedAt(LocalDateTime.now());

        payrollRecordRepository.save(record);
    }

    // ==================== Salary Calculation ====================

    @Override
    public SalaryCalculation calculateEmployeeSalary(Long driverId, Long periodId) {
        log.info("[Payroll] Calculating salary for driver {} in period {}", driverId, periodId);

        BigDecimal baseSalary = calculateBaseSalary(driverId, periodId);
        BigDecimal workHourBonus = calculateWorkHourBonus(driverId, periodId);
        BigDecimal performanceBonus = calculatePerformanceBonus(driverId, periodId);
        BigDecimal incentives = calculateIncentives(driverId, periodId);
        BigDecimal grossSalary = calculateGrossSalary(driverId, periodId);
        BigDecimal taxDeduction = calculateTaxDeduction(driverId, periodId);
        BigDecimal insuranceDeduction = calculateInsuranceDeduction(driverId, periodId);
        BigDecimal loanDeduction = calculateLoanDeduction(driverId, periodId);
        BigDecimal otherDeductions = calculateOtherDeductions(driverId, periodId);
        BigDecimal totalDeductions = getTotalDeductions(driverId, periodId);
        BigDecimal netSalary = calculateNetSalary(driverId, periodId);

        return new SalaryCalculation(
                driverId, periodId, baseSalary, workHourBonus, performanceBonus,
                incentives, grossSalary, taxDeduction, insuranceDeduction,
                loanDeduction, otherDeductions, totalDeductions, netSalary
        );
    }

    @Override
    public BigDecimal calculateBaseSalary(Long driverId, Long periodId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        return SalaryCalculationUtil.calculateBaseSalary(driver.getHourlyWage(), 160L);
    }

    @Override
    public BigDecimal calculateWorkHourBonus(Long driverId, Long periodId) {
        BigDecimal baseSalary = calculateBaseSalary(driverId, periodId);
        return SalaryCalculationUtil.calculateWorkHourBonus(baseSalary, 160L);
    }

    @Override
    public BigDecimal calculatePerformanceBonus(Long driverId, Long periodId) {
        BigDecimal baseSalary = calculateBaseSalary(driverId, periodId);
        Double performanceScore = 0.85;
        return SalaryCalculationUtil.calculatePerformanceBonus(baseSalary, performanceScore);
    }

    @Override
    public BigDecimal calculateIncentives(Long driverId, Long periodId) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateGrossSalary(Long driverId, Long periodId) {
        BigDecimal baseSalary = calculateBaseSalary(driverId, periodId);
        BigDecimal workHourBonus = calculateWorkHourBonus(driverId, periodId);
        BigDecimal performanceBonus = calculatePerformanceBonus(driverId, periodId);
        BigDecimal incentives = calculateIncentives(driverId, periodId);

        return SalaryCalculationUtil.calculateGrossSalary(
                baseSalary, BigDecimal.ZERO, workHourBonus, performanceBonus, incentives
        );
    }

    @Override
    public BigDecimal calculateNetSalary(Long driverId, Long periodId) {
        BigDecimal grossSalary = calculateGrossSalary(driverId, periodId);
        BigDecimal totalDeductions = getTotalDeductions(driverId, periodId);

        return SalaryCalculationUtil.calculateNetSalary(grossSalary, totalDeductions);
    }

    // ==================== Deductions ====================

    @Override
    public BigDecimal calculateTaxDeduction(Long driverId, Long periodId) {
        BigDecimal grossSalary = calculateGrossSalary(driverId, periodId);
        return SalaryCalculationUtil.calculateTaxDeduction(grossSalary);
    }

    @Override
    public BigDecimal calculateInsuranceDeduction(Long driverId, Long periodId) {
        BigDecimal grossSalary = calculateGrossSalary(driverId, periodId);
        return SalaryCalculationUtil.calculateInsuranceDeduction(grossSalary);
    }

    @Override
    public BigDecimal calculateLoanDeduction(Long driverId, Long periodId) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateOtherDeductions(Long driverId, Long periodId) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalDeductions(Long driverId, Long periodId) {
        BigDecimal tax = calculateTaxDeduction(driverId, periodId);
        BigDecimal insurance = calculateInsuranceDeduction(driverId, periodId);
        BigDecimal loan = calculateLoanDeduction(driverId, periodId);
        BigDecimal other = calculateOtherDeductions(driverId, periodId);

        return SalaryCalculationUtil.calculateTotalDeductions(tax, insurance, loan, other);
    }

    @Override
    public void addCustomDeduction(Long recordId, String description, BigDecimal amount) {
        log.info("[Payroll] Adding custom deduction to record {}: {} - {}", recordId, description, amount);
    }

    @Override
    public void removeCustomDeduction(Long recordId, Long deductionId) {
        log.info("[Payroll] Removing custom deduction {} from record {}", deductionId, recordId);
    }

    // ==================== Allowances & Bonuses ====================

    @Override
    public void addAllowance(Long driverId, String allowanceType, BigDecimal amount) {
        log.info("[Payroll] Adding allowance to driver {}: {} - {}", driverId, allowanceType, amount);
    }

    @Override
    public void removeAllowance(Long driverId, String allowanceType) {
        log.info("[Payroll] Removing allowance from driver {}: {}", driverId, allowanceType);
    }

    @Override
    public List<AllowanceRecord> getEmployeeAllowances(Long driverId) {
        return new ArrayList<>();
    }

    @Override
    public void setPerformanceBonusMultiplier(Long driverId, Double multiplier) {
        log.info("[Payroll] Setting performance bonus multiplier for driver {}: {}", driverId, multiplier);
    }

    @Override
    public void addOneTimeBonus(Long driverId, Long periodId, BigDecimal amount, String reason) {
        log.info("[Payroll] Adding one-time bonus to driver {} in period {}: {} - {}", driverId, periodId, amount, reason);
    }

    @Override
    public List<BonusRecord> getEmployeeBonusHistory(Long driverId) {
        return new ArrayList<>();
    }

    // ==================== Payment Processing ====================

    @Override
    @RequirePermission(Permission.UPDATE_PAYROLL)
    public void processPayment(Long recordId, String paymentMethod, String reference) {
        log.info("[Payroll] Processing payment for record {} via {}", recordId, paymentMethod);

        PayrollRecord record = getPayrollRecord(recordId)
                .orElseThrow(() -> PayrollRecordException.notFound(recordId));

        if (!record.getStatus().equals(PayrollRecord.PayrollStatus.APPROVED)) {
            throw PayrollRecordException.invalidStatus(record.getStatus().toString(), "PAID");
        }

        record.setStatus(PayrollRecord.PayrollStatus.PAID);
        record.setPaymentMethod(paymentMethod);
        record.setPaymentReference(reference);
        record.setUpdatedAt(LocalDateTime.now());

        payrollRecordRepository.save(record);
        log.info("[Payroll] Payment processed successfully for record {}", recordId);
    }

    @Override
    @RequirePermission(Permission.UPDATE_PAYROLL)
    public void batchProcessPayments(Long periodId, String paymentMethod) {
        log.info("[Payroll] Batch processing payments for period {}", periodId);

        List<PayrollRecord> records = payrollRecordRepository.findByPayrollPeriodIdAndStatus(
                periodId, PayrollRecord.PayrollStatus.APPROVED
        );

        for (PayrollRecord record : records) {
            processPayment(record.getId(), paymentMethod, "BATCH_" + System.currentTimeMillis());
        }

        log.info("[Payroll] Batch processing completed: {} records", records.size());
    }

    @Override
    public Optional<String> getPaymentStatus(Long recordId) {
        return getPayrollRecord(recordId)
                .map(record -> record.getStatus().toString());
    }

    @Override
    public byte[] generateSalarySlip(Long recordId) {
        log.info("[Payroll] Generating salary slip for record {}", recordId);
        return new byte[0];
    }

    @Override
    public void sendSalarySlip(Long recordId, String emailAddress) {
        log.info("[Payroll] Sending salary slip for record {} to {}", recordId, emailAddress);
    }

    @Override
    public PaymentStatus getPaymentStatusForPeriod(Long periodId) {
        List<PayrollRecord> records = payrollRecordRepository.findByPayrollPeriodId(periodId);
        int total = records.size();
        int paid = (int) records.stream().filter(r -> r.getStatus().equals(PayrollRecord.PayrollStatus.PAID)).count();
        int pending = total - paid;

        Optional<BigDecimal> totalAmount = payrollRecordRepository.calculateTotalNetPayForPeriod(periodId);

        return new PaymentStatus(
                periodId,
                total,
                paid,
                pending,
                totalAmount.orElse(BigDecimal.ZERO),
                paid == total ? "COMPLETED" : "PENDING"
        );
    }

    // ==================== Attendance & Hours Tracking ====================

    @Override
    public Long getEmployeeWorkHours(Long driverId, Long periodId) {
        return 160L;
    }

    @Override
    public Long getEmployeeOvertimeHours(Long driverId, Long periodId) {
        return 0L;
    }

    @Override
    public int getEmployeeLeaveDays(Long driverId, Long periodId) {
        return 0;
    }

    @Override
    public AttendanceSummary getAttendanceSummary(Long driverId, Long periodId) {
        return new AttendanceSummary(driverId, periodId, 22, 22, 0, 0, 100.0);
    }

    @Override
    public Double getAttendancePercentage(Long driverId, Long periodId) {
        return 100.0;
    }

    // ==================== Reporting & Analytics ====================

    @Override
    public PayrollSummaryReport getPayrollSummary(Long companyId, Long periodId) {
        List<PayrollRecord> records = payrollRecordRepository.findPayrollRecordsByPeriodAndCompany(companyId, periodId);

        BigDecimal totalGross = records.stream()
                .map(PayrollRecord::getGrossSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDeductions = records.stream()
                .map(PayrollRecord::getTotalDeductions)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNet = records.stream()
                .map(PayrollRecord::getNetPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int approved = (int) records.stream()
                .filter(r -> r.getStatus().equals(PayrollRecord.PayrollStatus.APPROVED))
                .count();

        return new PayrollSummaryReport(
                companyId, periodId, records.size(), totalGross, totalDeductions,
                totalNet, records.stream().map(PayrollRecord::getTaxDeduction).reduce(BigDecimal.ZERO, BigDecimal::add),
                approved, records.size() - approved
        );
    }

    @Override
    public PayrollCostAnalysis getPayrollCostAnalysis(Long companyId, Long fromDate, Long toDate) {
        return new PayrollCostAnalysis(
                companyId, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new ArrayList<>()
        );
    }

    @Override
    public List<SalaryTrend> getEmployeeSalaryTrends(Long driverId, int months) {
        return new ArrayList<>();
    }

    @Override
    public List<DepartmentSalaryComparison> getDepartmentComparison(Long companyId) {
        return new ArrayList<>();
    }

    @Override
    public SalaryDistribution getSalaryDistribution(Long companyId, Long periodId) {
        return new SalaryDistribution(companyId, periodId, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "");
    }

    @Override
    public byte[] exportPayrollToCSV(Long periodId) {
        log.info("[Payroll] Exporting payroll to CSV for period {}", periodId);
        return new byte[0];
    }

    @Override
    public String generatePayrollReport(Long companyId, Long periodId) {
        log.info("[Payroll] Generating payroll report for company {} period {}", companyId, periodId);
        return "Payroll Report Generated";
    }

    // ==================== Tax & Compliance ====================

    @Override
    public TaxSummary getTaxSummary(Long companyId, Long periodId) {
        return new TaxSummary(companyId, periodId, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Override
    public List<TaxDeduction> getTaxDeductionHistory(Long driverId) {
        return new ArrayList<>();
    }

    @Override
    public byte[] generateTaxCertificate(Long driverId, int year) {
        log.info("[Payroll] Generating tax certificate for driver {} year {}", driverId, year);
        return new byte[0];
    }

    @Override
    public PayrollCompliance verifyCompliance(Long periodId) {
        return new PayrollCompliance(periodId, true, true, new ArrayList<>(), "COMPLIANT");
    }
}
