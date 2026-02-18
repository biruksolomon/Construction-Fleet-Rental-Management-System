package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuspiciousFuelLog {

    private Long vehicleId;
    private String plateNumber;
    private Long logId;
    private String anomalyType;
    private String severity;
}

