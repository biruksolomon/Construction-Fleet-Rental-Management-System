package com.devcast.fleetmanagement.features.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * License Status DTO
 * Tracks license validity, expiration dates, and compliance status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicenseStatus {
    private Long driverId;
    private String licenseNumber;
    private String licenseType;
    private Long expiryDate;
    private boolean isValid;
    private Long daysUntilExpiry;
}
