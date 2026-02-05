package com.devcast.fleetmanagement.features.payroll.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SalaryCalculationUtil {
    
    private static final BigDecimal STANDARD_WORK_HOURS_MONTHLY = new BigDecimal("160");
    private static final BigDecimal OVERTIME_MULTIPLIER = new BigDecimal("1.5");
    private static final BigDecimal BONUS_MULTIPLIER_10_PERCENT = new BigDecimal("0.10");
    private static final BigDecimal BONUS_MULTIPLIER_15_PERCENT = new BigDecimal("0.15");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.15");
    private static final BigDecimal INSURANCE_RATE = new BigDecimal("0.05");
    
    public static BigDecimal calculateBaseSalary(BigDecimal hourlyRate, Long totalWorkHours) {
        return hourlyRate.multiply(new BigDecimal(totalWorkHours))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    public static BigDecimal calculateOvertimePay(BigDecimal hourlyRate, Long overtimeHours) {
        return hourlyRate.multiply(OVERTIME_MULTIPLIER)
                .multiply(new BigDecimal(overtimeHours))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    public static BigDecimal calculateWorkHourBonus(BigDecimal baseSalary, Long totalWorkHours) {
        if (totalWorkHours > 160) {
            return baseSalary.multiply(BONUS_MULTIPLIER_10_PERCENT)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    public static BigDecimal calculatePerformanceBonus(BigDecimal baseSalary, Double performanceScore) {
        if (performanceScore == null || performanceScore < 0.5) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal bonusPercent;
        if (performanceScore >= 0.9) {
            bonusPercent = BONUS_MULTIPLIER_15_PERCENT;
        } else if (performanceScore >= 0.75) {
            bonusPercent = new BigDecimal("0.12");
        } else {
            bonusPercent = new BigDecimal("0.05");
        }
        
        return baseSalary.multiply(bonusPercent)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    public static BigDecimal calculateGrossSalary(
            BigDecimal baseSalary,
            BigDecimal overtimePay,
            BigDecimal workHourBonus,
            BigDecimal performanceBonus,
            BigDecimal incentives) {
        
        return baseSalary.add(overtimePay)
                .add(workHourBonus)
                .add(performanceBonus)
                .add(incentives)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    public static BigDecimal calculateTaxDeduction(BigDecimal grossSalary) {
        return grossSalary.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    public static BigDecimal calculateInsuranceDeduction(BigDecimal grossSalary) {
        return grossSalary.multiply(INSURANCE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    public static BigDecimal calculateTotalDeductions(
            BigDecimal taxDeduction,
            BigDecimal insuranceDeduction,
            BigDecimal loanDeduction,
            BigDecimal otherDeductions) {
        
        return taxDeduction.add(insuranceDeduction)
                .add(loanDeduction)
                .add(otherDeductions)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    public static BigDecimal calculateNetSalary(BigDecimal grossSalary, BigDecimal totalDeductions) {
        return grossSalary.subtract(totalDeductions)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    public static BigDecimal calculateAttendancePercentage(int presentDays, int workDays) {
        if (workDays == 0) {
            return BigDecimal.ZERO;
        }
        
        return new BigDecimal(presentDays)
                .divide(new BigDecimal(workDays), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
