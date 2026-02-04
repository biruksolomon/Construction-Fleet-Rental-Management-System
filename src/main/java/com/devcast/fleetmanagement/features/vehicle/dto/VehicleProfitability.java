package com.devcast.fleetmanagement.features.vehicle.dto;

import java.math.BigDecimal;

public record VehicleProfitability(
        Long vehicleId,
        BigDecimal totalRevenue,
        BigDecimal totalCost,
        BigDecimal netProfit,
        Double profitMargin
) {}
