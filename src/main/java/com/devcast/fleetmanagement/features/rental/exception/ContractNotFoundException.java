package com.devcast.fleetmanagement.features.rental.exception;

/**
 * Contract Not Found Exception
 * Thrown when a rental contract cannot be found
 */
public class ContractNotFoundException extends RentalContractException {

    public ContractNotFoundException(String message) {
        super(message);
    }

    public ContractNotFoundException(Long contractId) {
        super("Rental contract not found with ID: " + contractId);
    }
}
