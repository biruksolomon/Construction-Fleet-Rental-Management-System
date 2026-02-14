package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FuelOptimizationTip {

    private String category;
    private String tip;
    private String expectedSaving;
    private String priority;
}

