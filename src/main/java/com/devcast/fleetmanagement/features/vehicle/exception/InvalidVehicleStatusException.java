package com.devcast.fleetmanagement.features.vehicle.exception;

/**
 * Invalid Vehicle Status Exception
 * Thrown when vehicle cannot perform operation due to invalid current status
 */
public class InvalidVehicleStatusException extends RuntimeException {

    private final String currentStatus;
    private final String attemptedOperation;

    public InvalidVehicleStatusException(String currentStatus, String attemptedOperation) {
        super("Cannot perform operation '" + attemptedOperation + "' on vehicle with status '" + currentStatus + "'");
        this.currentStatus = currentStatus;
        this.attemptedOperation = attemptedOperation;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getAttemptedOperation() {
        return attemptedOperation;
    }
}
