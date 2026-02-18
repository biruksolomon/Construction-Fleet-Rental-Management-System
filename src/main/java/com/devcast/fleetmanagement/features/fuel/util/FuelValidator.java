package com.devcast.fleetmanagement.features.fuel.util;

import com.devcast.fleetmanagement.features.fuel.model.FuelLog;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Fuel Validation Utility
 * Provides validation methods for fuel-related operations
 */
@Component
public class FuelValidator {

    /**
     * Validate fuel log entry
     * Checks for null values, positive amounts, and valid dates
     */
    public static void validateFuelLog(FuelLog fuelLog) {
        if (fuelLog == null) {
            throw new IllegalArgumentException("Fuel log cannot be null");
        }

        validateRefillDate(fuelLog.getRefillDate());
        validateLiters(fuelLog.getLiters());
        validateCost(fuelLog.getCost());
    }

    /**
     * Validate refill date
     * Date should not be in the future
     */
    public static void validateRefillDate(LocalDate refillDate) {
        if (refillDate == null) {
            throw new IllegalArgumentException("Refill date cannot be null");
        }

        if (refillDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Refill date cannot be in the future");
        }
    }

    /**
     * Validate liters amount
     * Must be positive and not exceed reasonable limits
     */
    public static void validateLiters(BigDecimal liters) {
        if (liters == null) {
            throw new IllegalArgumentException("Liters amount cannot be null");
        }

        if (liters.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Liters amount must be positive");
        }

        // Maximum 500 liters per refill (reasonable limit)
        if (liters.compareTo(BigDecimal.valueOf(500)) > 0) {
            throw new IllegalArgumentException("Liters amount exceeds maximum limit of 500");
        }
    }

    /**
     * Validate fuel cost
     * Must be non-negative (free fuel is valid in some cases)
     */
    public static void validateCost(BigDecimal cost) {
        if (cost == null) {
            throw new IllegalArgumentException("Cost cannot be null");
        }

        if (cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost cannot be negative");
        }

        // Maximum cost check (reasonable limit based on currency)
        if (cost.compareTo(BigDecimal.valueOf(10000)) > 0) {
            throw new IllegalArgumentException("Cost exceeds maximum limit");
        }
    }

    /**
     * Validate fuel budget
     * Budget must be positive
     */
    public static void validateFuelBudget(BigDecimal budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Budget cannot be null");
        }

        if (budget.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget must be positive");
        }

        // Maximum monthly budget (reasonable limit)
        if (budget.compareTo(BigDecimal.valueOf(100000)) > 0) {
            throw new IllegalArgumentException("Budget exceeds maximum limit");
        }
    }

    /**
     * Validate date range for queries
     * Start date should be before or equal to end date
     */
    public static void validateDateRange(Long fromDate, Long toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("Date range cannot have null values");
        }

        if (fromDate > toDate) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }

    /**
     * Validate consumption threshold
     * Threshold must be positive and reasonable
     */
    public static void validateConsumptionThreshold(double threshold) {
        if (threshold <= 0) {
            throw new IllegalArgumentException("Consumption threshold must be positive");
        }

        if (threshold > 100) {
            throw new IllegalArgumentException("Consumption threshold is unreasonably high");
        }
    }

    /**
     * Validate variance threshold for anomaly detection
     * Threshold must be between 0 and 500 percent
     */
    public static void validateVarianceThreshold(BigDecimal threshold) {
        if (threshold == null) {
            throw new IllegalArgumentException("Variance threshold cannot be null");
        }

        if (threshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Variance threshold cannot be negative");
        }

        if (threshold.compareTo(BigDecimal.valueOf(500)) > 0) {
            throw new IllegalArgumentException("Variance threshold exceeds maximum");
        }
    }

    /**
     * Validate fuel price per liter
     * Price must be positive and reasonable
     */
    public static void validateFuelPrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Fuel price cannot be null");
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Fuel price must be positive");
        }

        // Maximum price per liter (reasonable limit - typically $5-10)
        if (price.compareTo(BigDecimal.valueOf(50)) > 0) {
            throw new IllegalArgumentException("Fuel price exceeds maximum reasonable limit");
        }
    }

    /**
     * Validate fuel efficiency
     * Efficiency (km/liter) must be positive and reasonable
     */
    public static void validateFuelEfficiency(BigDecimal efficiency) {
        if (efficiency == null) {
            throw new IllegalArgumentException("Fuel efficiency cannot be null");
        }

        if (efficiency.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Fuel efficiency must be positive");
        }

        // Maximum efficiency (reasonable limit - typically 0.5-25 km/liter)
        if (efficiency.compareTo(BigDecimal.valueOf(50)) > 0) {
            throw new IllegalArgumentException("Fuel efficiency exceeds maximum reasonable limit");
        }
    }

    /**
     * Validate vehicle ID
     * Vehicle ID must be positive
     */
    public static void validateVehicleId(Long vehicleId) {
        if (vehicleId == null || vehicleId <= 0) {
            throw new IllegalArgumentException("Vehicle ID must be a positive number");
        }
    }

    /**
     * Validate company ID
     * Company ID must be positive
     */
    public static void validateCompanyId(Long companyId) {
        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("Company ID must be a positive number");
        }
    }

    /**
     * Validate pagination parameters
     */
    public static void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }

        if (size > 1000) {
            throw new IllegalArgumentException("Page size cannot exceed 1000");
        }
    }

    /**
     * Validate fuel price comparison
     * For detecting anomalies in fuel pricing
     */
    public static void validatePriceComparison(BigDecimal currentPrice, BigDecimal previousPrice) {
        if (currentPrice == null) {
            throw new IllegalArgumentException("Current price cannot be null");
        }

        if (previousPrice == null) {
            throw new IllegalArgumentException("Previous price cannot be null");
        }

        validateFuelPrice(currentPrice);
        validateFuelPrice(previousPrice);
    }

    /**
     * Validate consumption comparison
     * For detecting anomalies in fuel consumption
     */
    public static void validateConsumptionComparison(BigDecimal actual, BigDecimal expected) {
        if (actual == null) {
            throw new IllegalArgumentException("Actual consumption cannot be null");
        }

        if (expected == null) {
            throw new IllegalArgumentException("Expected consumption cannot be null");
        }

        if (actual.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Actual consumption cannot be negative");
        }

        if (expected.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Expected consumption cannot be negative");
        }
    }

    /**
     * Validate distance for consumption calculation
     * Distance must be positive
     */
    public static void validateDistance(BigDecimal distance) {
        if (distance == null) {
            throw new IllegalArgumentException("Distance cannot be null");
        }

        if (distance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Distance must be positive");
        }

        // Maximum distance in single trip
        if (distance.compareTo(BigDecimal.valueOf(10000)) > 0) {
            throw new IllegalArgumentException("Distance exceeds maximum reasonable limit");
        }
    }

    /**
     * Validate vendor name
     * Vendor name must not be empty
     */
    public static void validateVendorName(String vendorName) {
        if (vendorName == null || vendorName.trim().isEmpty()) {
            throw new IllegalArgumentException("Vendor name cannot be empty");
        }

        if (vendorName.length() > 255) {
            throw new IllegalArgumentException("Vendor name is too long");
        }
    }

    /**
     * Validate recorded by type
     * Must be SYSTEM or MANUAL
     */
    public static void validateRecordedByType(FuelLog.RecordedBy recordedBy) {
        if (recordedBy == null) {
            throw new IllegalArgumentException("Recorded by type cannot be null");
        }
    }

    /**
     * Check if fuel amount is suspicious (potential theft)
     */
    public static boolean isSuspiciousFuelAmount(BigDecimal amount, BigDecimal averageAmount) {
        if (averageAmount == null || averageAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // Flag if amount is 2x the average
        return amount.compareTo(averageAmount.multiply(BigDecimal.valueOf(2))) > 0;
    }

    /**
     * Check if fuel consumption is abnormal
     */
    public static boolean isAbnormalConsumption(BigDecimal actual, BigDecimal expected) {
        if (expected == null || expected.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // Flag if actual is 20% more than expected
        BigDecimal threshold = expected.multiply(BigDecimal.valueOf(1.2));
        return actual.compareTo(threshold) > 0;
    }
}
