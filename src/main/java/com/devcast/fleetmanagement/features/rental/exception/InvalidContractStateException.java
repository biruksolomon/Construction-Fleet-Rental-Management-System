package com.devcast.fleetmanagement.features.rental.exception;

/**
 * Invalid Contract State Exception
 * Thrown when operation cannot be performed due to contract's current state
 */
public class InvalidContractStateException extends RentalContractException {

    public InvalidContractStateException(String message) {
        super(message);
    }

    public InvalidContractStateException(String status, String operation) {
        super("Cannot " + operation + " contract in status: " + status);
    }
}
