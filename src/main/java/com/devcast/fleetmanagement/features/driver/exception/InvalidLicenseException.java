package com.devcast.fleetmanagement.features.driver.exception;

/**
 * Exception thrown when driver license is invalid or expired
 */
public class InvalidLicenseException extends RuntimeException {
    public InvalidLicenseException(String message) {
        super(message);
    }

    public InvalidLicenseException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidLicenseException expired(String licenseNumber) {
        return new InvalidLicenseException("License expired: " + licenseNumber);
    }

    public static InvalidLicenseException invalid() {
        return new InvalidLicenseException("Driver license is invalid");
    }
}
