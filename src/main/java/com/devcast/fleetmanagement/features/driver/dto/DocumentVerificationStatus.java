package com.devcast.fleetmanagement.features.driver.dto;

public record DocumentVerificationStatus(
        Long driverId,
        String licenseStatus,
        String insuranceStatus,
        String backgroundCheckStatus,
        Long lastVerificationDate,
        boolean allDocumentsValid
) {}