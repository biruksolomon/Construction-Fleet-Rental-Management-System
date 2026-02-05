package com.devcast.fleetmanagement.features.payroll.dto;

import com.devcast.fleetmanagement.features.payroll.model.PayrollRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRecordResponse {
    
    private Long id;
    private Long driverId;
    private String driverName;
    private Long payrollPeriodId;
    private Long totalWorkHours;
    private Long overtimeHours;
    private BigDecimal basePay;
    private BigDecimal workHourBonus;
    private BigDecimal performanceBonus;
    private BigDecimal incentives;
    private BigDecimal grossSalary;
    private BigDecimal taxDeduction;
    private BigDecimal insuranceDeduction;
    private BigDecimal loanDeduction;
    private BigDecimal otherDeductions;
    private BigDecimal totalDeductions;
    private BigDecimal netPay;
    private String status;
    private String paymentMethod;
    private String approvedBy;
    private LocalDateTime approvedDate;
    
    public static PayrollRecordResponse fromEntity(PayrollRecord record) {
        return PayrollRecordResponse.builder()
                .id(record.getId())
                .driverId(record.getDriver().getId())
                .driverName(record.getDriver().getUser().getFullName())
                .payrollPeriodId(record.getPayrollPeriod().getId())
                .totalWorkHours(record.getTotalWorkHours())
                .overtimeHours(record.getOvertimeHours())
                .basePay(record.getBasePay())
                .workHourBonus(record.getWorkHourBonus())
                .performanceBonus(record.getPerformanceBonus())
                .incentives(record.getIncentives())
                .grossSalary(record.getGrossSalary())
                .taxDeduction(record.getTaxDeduction())
                .insuranceDeduction(record.getInsuranceDeduction())
                .loanDeduction(record.getLoanDeduction())
                .otherDeductions(record.getOtherDeductions())
                .totalDeductions(record.getTotalDeductions())
                .netPay(record.getNetPay())
                .status(record.getStatus().toString())
                .paymentMethod(record.getPaymentMethod())
                .approvedBy(record.getApprovedBy())
                .approvedDate(record.getApprovedDate())
                .build();
    }
}
