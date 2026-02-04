package com.devcast.fleetmanagement.features.vehicle.dto;

import java.math.BigDecimal;

public record FleetHealthStatus(
        Long companyId,
        Integer totalVehicles,
        Integer activeVehicles,
        Integer maintenanceVehicles,
        Double avgFuelConsumption,
        BigDecimal avgMaintenanceCost,
        String healthRating
) {}