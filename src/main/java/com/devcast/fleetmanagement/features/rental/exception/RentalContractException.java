package com.devcast.fleetmanagement.features.rental.exception;

/**
 * Rental Contract Exception
 * Base exception for rental-related errors
 */
public class RentalContractException extends RuntimeException {

    public RentalContractException(String message) {
        super(message);
    }

    public RentalContractException(String message, Throwable cause) {
        super(message, cause);
    }
}
