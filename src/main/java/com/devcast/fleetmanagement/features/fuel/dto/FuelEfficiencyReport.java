package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FuelEfficiencyReport {

    private Long vehicleId;
    private BigDecimal avgConsumption;
    private BigDecimal bestConsumption;
    private BigDecimal worstConsumption;
    private String trend;
    private String recommendation;
}
