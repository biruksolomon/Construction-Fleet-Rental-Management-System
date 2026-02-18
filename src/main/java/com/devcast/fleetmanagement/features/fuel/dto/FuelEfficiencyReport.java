package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FuelEfficiencyReport {

    private Long vehicleId;
    private BigDecimal totalConsumption;
    private BigDecimal totalCost;
    private BigDecimal efficiencyRating;
    private String trend;
    private String recommendation;
}
