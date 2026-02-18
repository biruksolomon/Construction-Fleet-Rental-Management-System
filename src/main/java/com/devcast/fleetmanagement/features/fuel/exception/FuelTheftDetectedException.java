package com.devcast.fleetmanagement.features.fuel.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when potential fuel theft is detected
 */
public class FuelTheftDetectedException extends FuelManagementException {

    private final Long vehicleId;
    private final BigDecimal suspectedAmount;
    private final String severity;

    public FuelTheftDetectedException(Long vehicleId, BigDecimal suspectedAmount, String severity) {
        super(String.format("Potential fuel theft detected for vehicle %d: %s liters (%s)",
                vehicleId, suspectedAmount, severity));
        this.vehicleId = vehicleId;
        this.suspectedAmount = suspectedAmount;
        this.severity = severity;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public BigDecimal getSuspectedAmount() {
        return suspectedAmount;
    }

    public String getSeverity() {
        return severity;
    }
}
