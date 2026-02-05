package com.devcast.fleetmanagement.features.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Driver Incident DTO
 * Records safety-related incidents involving drivers with severity and details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverIncident {
    private Long incidentId;
    private Long driverId;
    private String type;
    private String description;
    private Long date;
    private String severity;
}
