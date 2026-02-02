package com.devcast.fleetmanagement.features.driver.dto;

import java.math.BigDecimal;

public  record DriverSalaryComponents(
        Long driverId,
        BigDecimal baseSalary,
        BigDecimal workHourBonus,
        BigDecimal performanceBonus,
        BigDecimal incentives,
        BigDecimal deductions,
        BigDecimal netSalary
) {}