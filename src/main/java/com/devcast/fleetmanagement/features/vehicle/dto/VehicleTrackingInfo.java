package com.devcast.fleetmanagement.features.vehicle.dto;

import java.math.BigDecimal;

public record VehicleTrackingInfo(
        Long vehicleId,
        String registrationNumber,
        Double latitude,
        Double longitude,
        String status,
        Long lastUpdate,
        BigDecimal fuelLevel,
        Long odometer
) {}

