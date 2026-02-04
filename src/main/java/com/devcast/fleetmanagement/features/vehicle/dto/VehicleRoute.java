package com.devcast.fleetmanagement.features.vehicle.dto;

import java.util.List;

public record VehicleRoute(
        Long vehicleId,
        List<GpsPoint> points,
        Double totalDistance,
        Long totalDuration
) {
    public record GpsPoint(Double latitude, Double longitude, Long timestamp) {}
}