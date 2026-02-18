package com.devcast.fleetmanagement.features.fuel.exception;

/**
 * Exception thrown when a fuel log is not found
 */
public class FuelLogNotFoundException extends FuelManagementException {

    public FuelLogNotFoundException(Long logId) {
        super("Fuel log not found: " + logId);
    }

    public FuelLogNotFoundException(String message) {
        super(message);
    }

    public static FuelLogNotFoundException forLogId(Long logId) {
        return new FuelLogNotFoundException(logId);
    }
}
