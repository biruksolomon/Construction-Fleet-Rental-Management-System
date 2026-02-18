package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class FuelSpike {

    private Long logId;
    private LocalDate refillDate;
    private BigDecimal previousConsumption;
    private BigDecimal currentConsumption;
    private BigDecimal increasePercentage;
}

