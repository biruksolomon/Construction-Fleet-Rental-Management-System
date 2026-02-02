package com.devcast.fleetmanagement.features.driver.dto;

public record LicenseStatus(
        Long driverId,
        String licenseNumber,
        String licenseType,
        Long expiryDate,
        boolean isValid,
        Long daysUntilExpiry
) {}