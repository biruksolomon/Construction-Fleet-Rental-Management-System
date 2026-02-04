package com.devcast.fleetmanagement.features.vehicle.dto;

import java.math.BigDecimal;

public record VehicleComparison(
        Long vehicleId,
        String registrationNumber,
        BigDecimal revenue,
        BigDecimal cost,
        BigDecimal profit,
        Double profitMargin,
        Long usageHours
) {}