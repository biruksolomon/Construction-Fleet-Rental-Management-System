package com.devcast.fleetmanagement.features.driver.util;

import com.devcast.fleetmanagement.features.driver.dto.DriverSalaryComponents;
import lombok.experimental.UtilityClass;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Driver Calculation Utility
 * Provides calculations for driver salaries, bonuses, and performance metrics
 */
@UtilityClass
public class DriverCalculationUtil {

    /**
     * Calculate days until license expiry
     */
    public static long calculateDaysUntilExpiry(LocalDate expiryDate) {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * Check if license is expired
     */
    public static boolean isLicenseExpired(LocalDate expiryDate) {
        return LocalDate.now().isAfter(expiryDate);
    }

    /**
     * Check if license will expire within specified days
     */
    public static boolean willExpireWithin(LocalDate expiryDate, int days) {
        long daysUntilExpiry = calculateDaysUntilExpiry(expiryDate);
        return daysUntilExpiry >= 0 && daysUntilExpiry <= days;
    }

    /**
     * Calculate performance score based on multiple metrics
     */
    public static double calculatePerformanceScore(double safetyScore, double efficiencyScore, double punctualityScore) {
        BigDecimal safety = BigDecimal.valueOf(safetyScore).multiply(BigDecimal.valueOf(0.40));
        BigDecimal efficiency = BigDecimal.valueOf(efficiencyScore).multiply(BigDecimal.valueOf(0.35));
        BigDecimal punctuality = BigDecimal.valueOf(punctualityScore).multiply(BigDecimal.valueOf(0.25));
        
        return safety.add(efficiency).add(punctuality).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Calculate performance rating based on score
     */
    public static String getPerformanceRating(double score) {
        if (score >= 90) return "EXCELLENT";
        if (score >= 75) return "GOOD";
        if (score >= 60) return "AVERAGE";
        if (score >= 45) return "BELOW_AVERAGE";
        return "POOR";
    }

    /**
     * Calculate salary based on work hours and hourly wage
     */
    public static BigDecimal calculateBaseSalary(Long workHours, BigDecimal hourlyWage) {
        return BigDecimal.valueOf(workHours).multiply(hourlyWage).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate bonus based on hours worked
     */
    public static BigDecimal calculateWorkHourBonus(Long workHours, BigDecimal baseSalary) {
        if (workHours > 160) { // More than standard 40-hour week (4 weeks)
            BigDecimal bonus = baseSalary.multiply(BigDecimal.valueOf(0.10));
            return bonus.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calculate performance bonus based on rating
     */
    public static BigDecimal calculatePerformanceBonus(double performanceScore, BigDecimal baseSalary) {
        BigDecimal multiplier;
        if (performanceScore >= 90) {
            multiplier = BigDecimal.valueOf(0.15); // 15% bonus
        } else if (performanceScore >= 75) {
            multiplier = BigDecimal.valueOf(0.10); // 10% bonus
        } else if (performanceScore >= 60) {
            multiplier = BigDecimal.valueOf(0.05); // 5% bonus
        } else {
            return BigDecimal.ZERO;
        }
        return baseSalary.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate total salary components
     */
    public static DriverSalaryComponents calculateSalaryComponents(
            Long workHours, BigDecimal hourlyWage, double performanceScore, 
            BigDecimal incentives, BigDecimal deductions) {
        
        BigDecimal baseSalary = calculateBaseSalary(workHours, hourlyWage);
        BigDecimal workHourBonus = calculateWorkHourBonus(workHours, baseSalary);
        BigDecimal performanceBonus = calculatePerformanceBonus(performanceScore, baseSalary);
        
        BigDecimal totalIncentives = incentives != null ? incentives : BigDecimal.ZERO;
        BigDecimal totalDeductions = deductions != null ? deductions : BigDecimal.ZERO;
        
        BigDecimal netSalary = baseSalary.add(workHourBonus).add(performanceBonus)
                .add(totalIncentives).subtract(totalDeductions).setScale(2, RoundingMode.HALF_UP);
        
        return DriverSalaryComponents.builder()
                .baseSalary(baseSalary)
                .workHourBonus(workHourBonus)
                .performanceBonus(performanceBonus)
                .incentives(totalIncentives)
                .deductions(totalDeductions)
                .netSalary(netSalary)
                .build();
    }
}
