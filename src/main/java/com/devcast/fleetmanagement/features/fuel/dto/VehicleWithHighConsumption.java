package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VehicleWithHighConsumption {

    private Long vehicleId;
    private String registrationNumber;
    private BigDecimal consumption;
    private BigDecimal percentageAboveAvg;
    private String recommendation;
}

