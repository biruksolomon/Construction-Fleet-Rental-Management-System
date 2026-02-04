package com.devcast.fleetmanagement.features.vehicle.util;

import com.devcast.fleetmanagement.features.vehicle.dto.VehicleCreateRequest;
import com.devcast.fleetmanagement.features.vehicle.dto.VehicleUpdateRequest;
import org.springframework.stereotype.Component;

/**
 * Vehicle Validator
 * Provides business logic validation for vehicles
 */
@Component
public class VehicleValidator {

    /**
     * Validate vehicle creation request
     */
    public void validateCreateRequest(VehicleCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Vehicle creation request cannot be null");
        }

        // Validate plate number format
        validatePlateNumber(request.getPlateNumber());

        // Validate asset code
        if (request.getAssetCode() == null || request.getAssetCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Asset code is required");
        }

        if (request.getAssetCode().length() < 2 || request.getAssetCode().length() > 100) {
            throw new IllegalArgumentException("Asset code must be between 2 and 100 characters");
        }

        // Validate rates
        if (request.getHourlyRate() != null && request.getHourlyRate().signum() <= 0) {
            throw new IllegalArgumentException("Hourly rate must be greater than 0");
        }

        if (request.getDailyRate() != null && request.getDailyRate().signum() <= 0) {
            throw new IllegalArgumentException("Daily rate must be greater than 0");
        }

        // Validate capabilities
        if (request.getHasGps() == null) {
            throw new IllegalArgumentException("GPS capability must be specified");
        }

        if (request.getHasFuelSensor() == null) {
            throw new IllegalArgumentException("Fuel sensor capability must be specified");
        }
    }

    /**
     * Validate vehicle update request
     */
    public void validateUpdateRequest(VehicleUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Vehicle update request cannot be null");
        }

        // Validate plate number if provided
        if (request.getPlateNumber() != null && !request.getPlateNumber().isEmpty()) {
            validatePlateNumber(request.getPlateNumber());
        }

        // Validate asset code if provided
        if (request.getAssetCode() != null && !request.getAssetCode().isEmpty()) {
            if (request.getAssetCode().length() < 2 || request.getAssetCode().length() > 100) {
                throw new IllegalArgumentException("Asset code must be between 2 and 100 characters");
            }
        }

        // Validate rates if provided
        if (request.getHourlyRate() != null && request.getHourlyRate().signum() <= 0) {
            throw new IllegalArgumentException("Hourly rate must be greater than 0");
        }

        if (request.getDailyRate() != null && request.getDailyRate().signum() <= 0) {
            throw new IllegalArgumentException("Daily rate must be greater than 0");
        }

        // Validate description if provided
        if (request.getDescription() != null && request.getDescription().length() > 500) {
            throw new IllegalArgumentException("Description must not exceed 500 characters");
        }

        // Validate license plate region if provided
        if (request.getLicensePlateRegion() != null) {
            validateLicensePlateRegion(request.getLicensePlateRegion());
        }
    }

    /**
     * Validate plate number format
     */
    private void validatePlateNumber(String plateNumber) {
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Plate number is required");
        }

        if (plateNumber.length() < 2 || plateNumber.length() > 50) {
            throw new IllegalArgumentException("Plate number must be between 2 and 50 characters");
        }

        // Basic format check - alphanumeric with optional hyphen/space
        if (!plateNumber.matches("^[A-Z0-9\\s\\-]+$")) {
            throw new IllegalArgumentException("Plate number contains invalid characters");
        }
    }

    /**
     * Validate license plate region code
     */
    private void validateLicensePlateRegion(String region) {
        if (region == null || region.isEmpty()) {
            return;
        }

        if (!region.matches("^[A-Z]{2}$")) {
            throw new IllegalArgumentException("License plate region must be exactly 2 uppercase letters");
        }
    }

    /**
     * Validate rates are consistent
     */
    public void validateRateConsistency(java.math.BigDecimal hourlyRate, java.math.BigDecimal dailyRate) {
        if (hourlyRate == null || dailyRate == null) {
            return;
        }

        // Daily rate should typically be higher than hourly rate * 8
        java.math.BigDecimal expectedMinDailyRate = hourlyRate.multiply(new java.math.BigDecimal("8"));
        if (dailyRate.compareTo(expectedMinDailyRate) < 0) {
            throw new IllegalArgumentException(
                    "Daily rate should be at least 8 times the hourly rate");
        }
    }
}
