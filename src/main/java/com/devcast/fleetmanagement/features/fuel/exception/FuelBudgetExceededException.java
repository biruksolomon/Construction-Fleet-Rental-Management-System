package com.devcast.fleetmanagement.features.fuel.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when fuel budget is exceeded
 */
public class FuelBudgetExceededException extends FuelManagementException {

    private final Long vehicleId;
    private final BigDecimal budget;
    private final BigDecimal spent;
    private final BigDecimal overage;

    public FuelBudgetExceededException(Long vehicleId, BigDecimal budget, BigDecimal spent) {
        super(String.format("Fuel budget exceeded for vehicle %d: budget %.2f, spent %.2f",
                vehicleId, budget, spent));
        this.vehicleId = vehicleId;
        this.budget = budget;
        this.spent = spent;
        this.overage = spent.subtract(budget);
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public BigDecimal getOverage() {
        return overage;
    }
}
