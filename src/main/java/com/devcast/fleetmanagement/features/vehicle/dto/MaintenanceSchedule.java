package com.devcast.fleetmanagement.features.vehicle.dto;

public record MaintenanceSchedule(
        Long vehicleId,
        String serviceType,
        Long dueMileage,
        Long dueDate,
        String status
) {}