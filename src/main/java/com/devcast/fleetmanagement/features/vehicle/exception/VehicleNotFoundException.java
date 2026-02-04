package com.devcast.fleetmanagement.features.vehicle.exception;

/**
 * Vehicle Not Found Exception
 * Thrown when a vehicle cannot be found by ID or other unique identifier
 */
public class VehicleNotFoundException extends RuntimeException {

    private final Long vehicleId;

    public VehicleNotFoundException(Long vehicleId) {
        super("Vehicle not found with ID: " + vehicleId);
        this.vehicleId = vehicleId;
    }

    public VehicleNotFoundException(String message) {
        super(message);
        this.vehicleId = null;
    }

    public VehicleNotFoundException(String message, Long vehicleId) {
        super(message);
        this.vehicleId = vehicleId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }
}
