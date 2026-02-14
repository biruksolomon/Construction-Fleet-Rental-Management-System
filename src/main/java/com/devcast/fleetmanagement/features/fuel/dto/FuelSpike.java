package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FuelSpike {

    private Long vehicleId;
    private Long logId;
    private BigDecimal spike;
    private String explanation;
    private String action;
}

