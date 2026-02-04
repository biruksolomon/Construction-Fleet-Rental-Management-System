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
public class FuelAnomaly {
    private Long vehicleId;
    private Long logId;
    private String anomalyType;
    private BigDecimal variance;
    private String recommendation;
}
