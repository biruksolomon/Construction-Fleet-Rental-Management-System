package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class FuelTheftAlert {

    private Long vehicleId;
    private String plateNumber;
    private Long logId;
    private BigDecimal suspectedTheftLiters;
    private LocalDateTime alertDate;
}
