package com.devcast.fleetmanagement.features.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Driver Salary Components DTO
 * Breaks down driver salary into base pay, bonuses, incentives, and deductions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverSalaryComponents {
    private Long driverId;
    private BigDecimal baseSalary;
    private BigDecimal workHourBonus;
    private BigDecimal performanceBonus;
    private BigDecimal incentives;
    private BigDecimal deductions;
    private BigDecimal netSalary;
}
