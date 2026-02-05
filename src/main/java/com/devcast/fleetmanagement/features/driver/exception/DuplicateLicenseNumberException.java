package com.devcast.fleetmanagement.features.driver.exception;

/**
 * Exception thrown when duplicate license number is used
 */
public class DuplicateLicenseNumberException extends RuntimeException {
    public DuplicateLicenseNumberException(String message) {
        super(message);
    }

    public DuplicateLicenseNumberException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DuplicateLicenseNumberException forLicense(String licenseNumber) {
        return new DuplicateLicenseNumberException("License number already exists: " + licenseNumber);
    }
}
