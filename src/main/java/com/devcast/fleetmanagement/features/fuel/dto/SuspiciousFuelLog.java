package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuspiciousFuelLog {

    private Long logId;
    private Long vehicleId;
    private String issue;
    private String severity;
    private Long date;
}

