package com.devcast.fleetmanagement.features.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Driver Filter Criteria DTO
 * Used for advanced filtering and searching drivers based on multiple criteria
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverFilterCriteria {
    private String status;
    private String licenseType;
    private Double minRating;
    private Long fromDate;
    private Long toDate;
}
