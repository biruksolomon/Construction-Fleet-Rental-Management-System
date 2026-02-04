package com.devcast.fleetmanagement.features.vehicle.dto;

import java.math.BigDecimal;

public record FuelAnomaly(
        Long vehicleId,
        Long logId,
        String anomalyType,
        BigDecimal variance,
        String recommendation
) {}
