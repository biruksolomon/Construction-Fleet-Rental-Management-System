package com.devcast.fleetmanagement.features.driver.dto;

import com.devcast.fleetmanagement.features.driver.model.Driver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Driver Response DTO
 * Complete driver information for API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private Long companyId;
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private String licenseType;
    private BigDecimal hourlyWage;
    private String employmentType;
    private String status;
    private String insuranceNumber;
    private LocalDate insuranceExpiry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert Driver entity to Response DTO
     */
    public static DriverResponse fromEntity(Driver driver) {
        return DriverResponse.builder()
                .id(driver.getId())
                .userId(driver.getUser().getId())
                .userFullName(driver.getUser().getFullName())
                .userEmail(driver.getUser().getEmail())
                .companyId(driver.getCompany().getId())
                .licenseNumber(driver.getLicenseNumber())
                .licenseExpiry(driver.getLicenseExpiry())
                .hourlyWage(driver.getHourlyWage())
                .employmentType(driver.getEmploymentType().name())
                .status(driver.getStatus().name())
                .build();
    }
}
