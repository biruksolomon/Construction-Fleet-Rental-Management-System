package com.devcast.fleetmanagement.features.driver.util;

import com.devcast.fleetmanagement.features.driver.exception.DuplicateLicenseNumberException;
import com.devcast.fleetmanagement.features.driver.exception.InvalidLicenseException;
import lombok.experimental.UtilityClass;
import java.time.LocalDate;

/**
 * Driver Validation Utility
 * Validates driver data and license information
 */
@UtilityClass
public class DriverValidator {

    /**
     * Validate license number format
     */
    public static boolean isValidLicenseFormat(String licenseNumber) {
        return licenseNumber != null && licenseNumber.length() >= 5 && licenseNumber.length() <= 50;
    }

    /**
     * Validate license expiry date
     */
    public static void validateLicenseExpiry(LocalDate expiryDate) {
        if (expiryDate == null) {
            throw new IllegalArgumentException("License expiry date cannot be null");
        }
        if (expiryDate.isBefore(LocalDate.now())) {
            throw InvalidLicenseException.expired(expiryDate.toString());
        }
    }

    /**
     * Validate employment type
     */
    public static boolean isValidEmploymentType(String type) {
        return type != null && (type.equals("FULL_TIME") || type.equals("PART_TIME") || type.equals("CONTRACT"));
    }

    /**
     * Validate work limits
     */
    public static boolean isValidWorkLimit(Integer maxHoursPerDay, Integer maxHoursPerWeek) {
        return maxHoursPerDay != null && maxHoursPerDay > 0 && 
               maxHoursPerWeek != null && maxHoursPerWeek > 0 &&
               maxHoursPerWeek > maxHoursPerDay * 5; // Week limit should be more than 5 days
    }

    /**
     * Validate hourly wage
     */
    public static boolean isValidHourlyWage(java.math.BigDecimal wage) {
        return wage != null && wage.compareTo(java.math.BigDecimal.ZERO) > 0;
    }

    /**
     * Check if driver can work (license valid, not suspended)
     */
    public static boolean canDriverWork(String status, LocalDate licenseExpiry) {
        return status.equals("ACTIVE") && !DriverCalculationUtil.isLicenseExpired(licenseExpiry);
    }
}
