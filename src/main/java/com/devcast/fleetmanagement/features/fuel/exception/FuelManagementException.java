package com.devcast.fleetmanagement.features.fuel.exception;

/**
 * Base exception for fuel management operations
 */
public class FuelManagementException extends RuntimeException {

    public FuelManagementException(String message) {
        super(message);
    }

    public FuelManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}
