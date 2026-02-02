package com.devcast.fleetmanagement.features.payroll.service;

import com.devcast.fleetmanagement.features.payroll.model.PayrollPeriod;
import com.devcast.fleetmanagement.features.payroll.model.PayrollRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Payroll Service Interface
 * Handles payroll processing, salary calculation, and employee compensation
 */
public interface PayrollService {

    // ==================== Payroll Period Management ====================

    /**
     * Create payroll period
     */
    PayrollPeriod createPayrollPeriod(Long companyId, PayrollPeriod period);

    /**
     * Get payroll period by ID
     */
    Optional<PayrollPeriod> getPayrollPeriodById(Long periodId);

    /**
     * Get active payroll period
     */
    Optional<PayrollPeriod> getActivePayrollPeriod(Long companyId);

    /**
     * Get payroll periods in company
     */
    Page<PayrollPeriod> getPayrollPeriods(Long companyId, Pageable pageable);

    /**
     * Get payroll periods by status
     */
    List<PayrollPeriod> getPayrollPeriodsByStatus(Long companyId, String status);

    /**
     * Update payroll period
     */
    PayrollPeriod updatePayrollPeriod(Long periodId, PayrollPeriod period);

    /**
     * Close payroll period
     */
    void closePayrollPeriod(Long periodId);

    /**
     * Cancel payroll period
     */
    void cancelPayrollPeriod(Long periodId, String reason);

    /**
     * Lock payroll period
     */
    void lockPayrollPeriod(Long periodId);

    /**
     * Finalize payroll period
     */
    void finalizePayrollPeriod(Long periodId);

    // ==================== Payroll Record Operations ====================

    /**
     * Generate payroll records for period
     */
    List<PayrollRecord> generatePayrollRecords(Long periodId);

    /**
     * Get payroll record
     */
    Optional<PayrollRecord> getPayrollRecord(Long recordId);

    /**
     * Get payroll records for period
     */
    Page<PayrollRecord> getPayrollRecordsForPeriod(Long periodId, Pageable pageable);

    /**
     * Get payroll records for employee
     */
    List<PayrollRecord> getEmployeePayrollHistory(Long driverId, int months);

    /**
     * Update payroll record
     */
    PayrollRecord updatePayrollRecord(Long recordId, PayrollRecord record);

    /**
     * Approve payroll record
     */
    void approvePayrollRecord(Long recordId);

    /**
     * Reject payroll record
     */
    void rejectPayrollRecord(Long recordId, String reason);

    /**
     * Void payroll record
     */
    void voidPayrollRecord(Long recordId, String reason);

    // ==================== Salary Calculation ====================

    /**
     * Calculate employee salary
     */
    SalaryCalculation calculateEmployeeSalary(Long driverId, Long periodId);

    /**
     * Calculate base salary
     */
    BigDecimal calculateBaseSalary(Long driverId, Long periodId);

    /**
     * Calculate work hour bonus
     */
    BigDecimal calculateWorkHourBonus(Long driverId, Long periodId);

    /**
     * Calculate performance bonus
     */
    BigDecimal calculatePerformanceBonus(Long driverId, Long periodId);

    /**
     * Calculate incentives
     */
    BigDecimal calculateIncentives(Long driverId, Long periodId);

    /**
     * Calculate gross salary
     */
    BigDecimal calculateGrossSalary(Long driverId, Long periodId);

    /**
     * Calculate net salary
     */
    BigDecimal calculateNetSalary(Long driverId, Long periodId);

    // ==================== Deductions ====================

    /**
     * Calculate tax deduction
     */
    BigDecimal calculateTaxDeduction(Long driverId, Long periodId);

    /**
     * Calculate insurance deduction
     */
    BigDecimal calculateInsuranceDeduction(Long driverId, Long periodId);

    /**
     * Calculate loan deduction
     */
    BigDecimal calculateLoanDeduction(Long driverId, Long periodId);

    /**
     * Calculate other deductions
     */
    BigDecimal calculateOtherDeductions(Long driverId, Long periodId);

    /**
     * Get total deductions
     */
    BigDecimal getTotalDeductions(Long driverId, Long periodId);

    /**
     * Add custom deduction
     */
    void addCustomDeduction(Long recordId, String description, BigDecimal amount);

    /**
     * Remove custom deduction
     */
    void removeCustomDeduction(Long recordId, Long deductionId);

    // ==================== Allowances & Bonuses ====================

    /**
     * Add allowance to employee
     */
    void addAllowance(Long driverId, String allowanceType, BigDecimal amount);

    /**
     * Remove allowance
     */
    void removeAllowance(Long driverId, String allowanceType);

    /**
     * Get employee allowances
     */
    List<AllowanceRecord> getEmployeeAllowances(Long driverId);

    /**
     * Set performance bonus multiplier
     */
    void setPerformanceBonusMultiplier(Long driverId, Double multiplier);

    /**
     * Add one-time bonus
     */
    void addOneTimeBonus(Long driverId, Long periodId, BigDecimal amount, String reason);

    /**
     * Get bonus history
     */
    List<BonusRecord> getEmployeeBonusHistory(Long driverId);

    // ==================== Payment Processing ====================

    /**
     * Process payment for payroll record
     */
    void processPayment(Long recordId, String paymentMethod, String reference);

    /**
     * Batch process payments
     */
    void batchProcessPayments(Long periodId, String paymentMethod);

    /**
     * Get payment status
     */
    Optional<String> getPaymentStatus(Long recordId);

    /**
     * Generate salary slip
     */
    byte[] generateSalarySlip(Long recordId);

    /**
     * Send salary slip to employee
     */
    void sendSalarySlip(Long recordId, String emailAddress);

    /**
     * Get payment status for period
     */
    PaymentStatus getPaymentStatusForPeriod(Long periodId);

    // ==================== Attendance & Hours Tracking ====================

    /**
     * Get employee work hours
     */
    Long getEmployeeWorkHours(Long driverId, Long periodId);

    /**
     * Get employee overtime hours
     */
    Long getEmployeeOvertimeHours(Long driverId, Long periodId);

    /**
     * Get employee leave days
     */
    int getEmployeeLeaveDays(Long driverId, Long periodId);

    /**
     * Get employee attendance summary
     */
    AttendanceSummary getAttendanceSummary(Long driverId, Long periodId);

    /**
     * Get attendance percentage
     */
    Double getAttendancePercentage(Long driverId, Long periodId);

    // ==================== Reporting & Analytics ====================

    /**
     * Get payroll summary report
     */
    PayrollSummaryReport getPayrollSummary(Long companyId, Long periodId);

    /**
     * Get payroll cost analysis
     */
    PayrollCostAnalysis getPayrollCostAnalysis(Long companyId, Long fromDate, Long toDate);

    /**
     * Get employee salary trends
     */
    List<SalaryTrend> getEmployeeSalaryTrends(Long driverId, int months);

    /**
     * Get department salary comparison
     */
    List<DepartmentSalaryComparison> getDepartmentComparison(Long companyId);

    /**
     * Get salary distribution
     */
    SalaryDistribution getSalaryDistribution(Long companyId, Long periodId);

    /**
     * Export payroll to CSV
     */
    byte[] exportPayrollToCSV(Long periodId);

    /**
     * Generate payroll report
     */
    String generatePayrollReport(Long companyId, Long periodId);

    // ==================== Tax & Compliance ====================

    /**
     * Calculate tax summary
     */
    TaxSummary getTaxSummary(Long companyId, Long periodId);

    /**
     * Get tax deduction history
     */
    List<TaxDeduction> getTaxDeductionHistory(Long driverId);

    /**
     * Generate tax certificate
     */
    byte[] generateTaxCertificate(Long driverId, int year);

    /**
     * Verify payroll compliance
     */
    PayrollCompliance verifyCompliance(Long periodId);

    // Data Transfer Objects

    record SalaryCalculation(
            Long driverId,
            Long periodId,
            BigDecimal baseSalary,
            BigDecimal workHourBonus,
            BigDecimal performanceBonus,
            BigDecimal incentives,
            BigDecimal grossSalary,
            BigDecimal taxDeduction,
            BigDecimal insuranceDeduction,
            BigDecimal loanDeduction,
            BigDecimal otherDeductions,
            BigDecimal totalDeductions,
            BigDecimal netSalary
    ) {}

    record AllowanceRecord(
            String allowanceType,
            BigDecimal amount,
            Long effectiveDate
    ) {}

    record BonusRecord(
            Long bonusId,
            Long driverId,
            Long periodId,
            BigDecimal amount,
            String reason,
            Long date
    ) {}

    record PaymentStatus(
            Long periodId,
            int totalRecords,
            int paidRecords,
            int pendingRecords,
            BigDecimal totalAmount,
            String status
    ) {}

    record AttendanceSummary(
            Long driverId,
            Long periodId,
            int workDays,
            int presentDays,
            int absentDays,
            int leaveDays,
            Double attendancePercentage
    ) {}

    record PayrollSummaryReport(
            Long companyId,
            Long periodId,
            int totalEmployees,
            BigDecimal totalGrossSalary,
            BigDecimal totalDeductions,
            BigDecimal totalNetSalary,
            BigDecimal totalTax,
            int approvedRecords,
            int pendingApproval
    ) {}

    record PayrollCostAnalysis(
            Long companyId,
            BigDecimal totalPayroll,
            BigDecimal avgEmployeeSalary,
            BigDecimal payrollAsPercentOfRevenue,
            List<CostBreakdown> costBreakdown
    ) {
        public record CostBreakdown(String category, BigDecimal amount) {}
    }

    record SalaryTrend(
            Long date,
            BigDecimal salary,
            BigDecimal bonus,
            BigDecimal deductions
    ) {}

    record DepartmentSalaryComparison(
            String department,
            BigDecimal avgSalary,
            int employeeCount,
            BigDecimal totalPayroll
    ) {}

    record SalaryDistribution(
            Long companyId,
            Long periodId,
            int employees,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            BigDecimal avgSalary,
            String distribution
    ) {}

    record TaxSummary(
            Long companyId,
            Long periodId,
            BigDecimal totalTaxable,
            BigDecimal totalTax,
            BigDecimal avgTaxPerEmployee
    ) {}

    record TaxDeduction(
            Long deductionId,
            Long driverId,
            Long periodId,
            BigDecimal amount,
            Long date
    ) {}

    record PayrollCompliance(
            Long periodId,
            boolean isTaxCompliant,
            boolean isInsuranceCompliant,
            List<String> issues,
            String overallStatus
    ) {}
}
