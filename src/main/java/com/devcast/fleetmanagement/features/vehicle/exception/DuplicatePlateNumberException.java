package com.devcast.fleetmanagement.features.vehicle.exception;

/**
 * Duplicate Plate Number Exception
 * Thrown when a vehicle plate number already exists
 */
public class DuplicatePlateNumberException extends RuntimeException {

    private final String plateNumber;

    public DuplicatePlateNumberException(String plateNumber) {
        super("Vehicle with plate number already exists: " + plateNumber);
        this.plateNumber = plateNumber;
    }

    public String getPlateNumber() {
        return plateNumber;
    }
}
