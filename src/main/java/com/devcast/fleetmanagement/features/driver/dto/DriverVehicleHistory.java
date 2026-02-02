package com.devcast.fleetmanagement.features.driver.dto;

public  record DriverVehicleHistory(
        Long driverId,
        String driverName,
        Long vehicleId,
        String registrationNumber,
        Long assignmentDate,
        Long endDate,
        String status
) {}