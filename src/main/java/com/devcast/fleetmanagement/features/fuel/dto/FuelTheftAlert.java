package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FuelTheftAlert {

    private Long vehicleId;
    private String registrationNumber;
    private BigDecimal suspectedAmount;
    private String reasoning;
    private String severity;
}
