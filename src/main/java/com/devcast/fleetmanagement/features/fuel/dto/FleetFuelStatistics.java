package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FleetFuelStatistics {

    private BigDecimal totalConsumption;
    private BigDecimal totalCost;
    private BigDecimal averageCostPerLiter;
    private int vehicleCount;
}

