package com.devcast.fleetmanagement.features.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalarySlipResponse {
    
    private Long recordId;
    private String driverName;
    private String driverId;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private Long workingHours;
    private Long overtimeHours;
    private BigDecimal hourlyRate;
    private BigDecimal baseSalary;
    private BigDecimal workHourBonus;
    private BigDecimal performanceBonus;
    private BigDecimal incentives;
    private BigDecimal grossSalary;
    private BigDecimal taxDeduction;
    private BigDecimal insuranceDeduction;
    private BigDecimal loanDeduction;
    private BigDecimal otherDeductions;
    private BigDecimal totalDeductions;
    private BigDecimal netSalary;
    private String status;
    private String paymentMethod;
}
