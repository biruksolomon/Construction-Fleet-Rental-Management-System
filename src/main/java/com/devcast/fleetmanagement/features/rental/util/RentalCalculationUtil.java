package com.devcast.fleetmanagement.features.rental.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Rental Calculation Utilities
 * Helper class for common rental calculations and validations
 */
public class RentalCalculationUtil {

    /**
     * Calculate number of days between two dates (inclusive of both dates)
     */
    public static int calculateDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Calculate number of hours between two dates
     */
    public static long calculateHours(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(startDate.atStartOfDay(), endDate.atStartOfDay());
    }

    /**
     * Calculate rental cost based on rate and days
     */
    public static BigDecimal calculateCost(BigDecimal rate, int days) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return rate.multiply(BigDecimal.valueOf(days));
    }

    /**
     * Calculate cost with hourly rate
     */
    public static BigDecimal calculateHourlyCost(BigDecimal hourlyRate, long hours) {
        if (hourlyRate == null || hourlyRate.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return hourlyRate.multiply(BigDecimal.valueOf(hours));
    }

    /**
     * Apply discount to amount
     */
    public static BigDecimal applyDiscount(BigDecimal amount, BigDecimal discountPercent) {
        if (amount == null || discountPercent == null) {
            return amount;
        }
        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                discountPercent.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        );
        return amount.multiply(discountMultiplier);
    }

    /**
     * Calculate late fees (typically 1.5x daily rate per day)
     */
    public static BigDecimal calculateLateFees(BigDecimal dailyRate, int lateDays, BigDecimal lateFeeMultiplier) {
        if (dailyRate == null || lateFeeMultiplier == null) {
            return BigDecimal.ZERO;
        }
        return dailyRate.multiply(BigDecimal.valueOf(lateDays)).multiply(lateFeeMultiplier);
    }

    /**
     * Validate date range
     */
    public static boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !startDate.isAfter(endDate) && !startDate.isBefore(LocalDate.now());
    }

    /**
     * Check if contract is within rental period
     */
    public static boolean isWithinRentalPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * Check if rental has started
     */
    public static boolean hasRentalStarted(LocalDate startDate) {
        return !LocalDate.now().isBefore(startDate);
    }

    /**
     * Check if rental has ended
     */
    public static boolean hasRentalEnded(LocalDate endDate) {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * Calculate percentage utilization
     */
    public static double calculateUtilization(int usedDays, int totalDays) {
        if (totalDays <= 0) {
            return 0.0;
        }
        return (double) usedDays / totalDays * 100;
    }

    /**
     * Round BigDecimal to 2 decimal places
     */
    public static BigDecimal roundToTwoDecimals(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate prorated amount
     */
    public static BigDecimal calculateProrated(BigDecimal amount, int usedDays, int totalDays) {
        if (amount == null || totalDays <= 0) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(BigDecimal.valueOf(usedDays))
                .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);
    }
}
