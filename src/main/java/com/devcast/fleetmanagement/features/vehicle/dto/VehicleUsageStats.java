package com.devcast.fleetmanagement.features.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleUsageStats {
    private Long vehicleId;
    private Long totalUsageHours;
    private Long totalKilometers;
    private Integer tripCount;
    private BigDecimal averageTripDistance;
}
