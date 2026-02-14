package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VehicleFuelComparison {

    private Long vehicleId;
    private String registrationNumber;
    private BigDecimal avgConsumption;
    private BigDecimal totalCost;
    private String comparison;
}
