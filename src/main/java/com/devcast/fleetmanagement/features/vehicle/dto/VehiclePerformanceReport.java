package com.devcast.fleetmanagement.features.vehicle.dto;

import java.math.BigDecimal;

public record VehiclePerformanceReport(
        Long vehicleId,
        BigDecimal efficiency,
        BigDecimal reliability,
        BigDecimal utilization,
        BigDecimal profitability,
        String overallRating
) {}