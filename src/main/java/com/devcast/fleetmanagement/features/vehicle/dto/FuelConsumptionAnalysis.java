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
public class FuelConsumptionAnalysis {
    private Long vehicleId;
    private BigDecimal totalFuelConsumed;
    private Long totalDistance;
    private BigDecimal averageConsumption;
    private BigDecimal totalCost;
    private String trend;
}

