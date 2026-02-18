package com.devcast.fleetmanagement.features.fuel.exception;

/**
 * Exception thrown when fuel data is invalid
 */
public class InvalidFuelDataException extends FuelManagementException {

    public InvalidFuelDataException(String message) {
        super(message);
    }

    public InvalidFuelDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidFuelDataException invalidLiters(String reason) {
        return new InvalidFuelDataException("Invalid liters amount: " + reason);
    }

    public static InvalidFuelDataException invalidCost(String reason) {
        return new InvalidFuelDataException("Invalid fuel cost: " + reason);
    }

    public static InvalidFuelDataException invalidDate(String reason) {
        return new InvalidFuelDataException("Invalid refill date: " + reason);
    }
}
