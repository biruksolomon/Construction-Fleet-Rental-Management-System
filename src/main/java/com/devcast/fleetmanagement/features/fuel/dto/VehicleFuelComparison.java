package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VehicleFuelComparison {

    private Long vehicleId;
    private String plateNumber;
    private BigDecimal totalConsumption;
    private BigDecimal averageConsumption;
    private int logCount;
}
