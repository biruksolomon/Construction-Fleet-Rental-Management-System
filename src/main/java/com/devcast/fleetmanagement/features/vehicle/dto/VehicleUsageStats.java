package com.devcast.fleetmanagement.features.vehicle.dto;

import java.math.BigDecimal;

public record VehicleUsageStats(
        Long vehicleId,
        Long totalUsageHours,
        Long totalKilometers,
        Integer tripCount,
        BigDecimal averageTripDistance
) {}