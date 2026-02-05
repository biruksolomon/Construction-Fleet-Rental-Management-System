package com.devcast.fleetmanagement.features.driver.dto;

import com.devcast.fleetmanagement.features.driver.model.Driver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Driver Basic Response DTO
 * Lightweight driver information for list views and quick lookups
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverBasicResponse {
    private Long id;
    private String driverName;
    private String licenseNumber;
    private String status;
    private String contactInfo;

    /**
     * Convert Driver entity to Basic Response DTO
     */
    public static DriverBasicResponse fromEntity(Driver driver) {
        return DriverBasicResponse.builder()
                .id(driver.getId())
                .driverName(driver.getUser().getFullName())
                .licenseNumber(driver.getLicenseNumber())
                .status(driver.getStatus().name())
                .contactInfo(driver.getUser().getPhone())
                .build();
    }
}
