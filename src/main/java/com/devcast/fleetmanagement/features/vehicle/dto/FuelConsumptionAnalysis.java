package com.devcast.fleetmanagement.features.vehicle.dto;

import java.math.BigDecimal;

public record FuelConsumptionAnalysis(
        BigDecimal totalFuelConsumed,
        Long totalDistance,
        BigDecimal avgConsumption,
        BigDecimal totalCost,
        String trend
) {}

