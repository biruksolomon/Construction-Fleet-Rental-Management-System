package com.devcast.fleetmanagement.features.driver.exception;

/**
 * Exception thrown when a driver is not found
 */
public class DriverNotFoundException extends RuntimeException {
    public DriverNotFoundException(String message) {
        super(message);
    }

    public DriverNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DriverNotFoundException withId(Long driverId) {
        return new DriverNotFoundException("Driver not found with ID: " + driverId);
    }

    public static DriverNotFoundException withLicenseNumber(String licenseNumber) {
        return new DriverNotFoundException("Driver not found with license number: " + licenseNumber);
    }
}
