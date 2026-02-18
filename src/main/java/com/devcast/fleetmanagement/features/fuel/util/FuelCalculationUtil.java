package com.devcast.fleetmanagement.features.fuel.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Fuel Calculation Utility
 * Provides helper methods for fuel consumption calculations and analysis
 */
@Component
public class FuelCalculationUtil {

    // Constants
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Calculate fuel consumption per kilometer
     * Formula: Total Liters / Total Kilometers
     */
    public static BigDecimal calculateConsumptionPerKm(BigDecimal totalLiters, BigDecimal totalKm) {
        if (totalKm == null || totalKm.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return totalLiters.divide(totalKm, SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate fuel cost per kilometer
     * Formula: Total Cost / Total Kilometers
     */
    public static BigDecimal calculateCostPerKm(BigDecimal totalCost, BigDecimal totalKm) {
        if (totalKm == null || totalKm.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return totalCost.divide(totalKm, SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate average fuel price per liter
     * Formula: Total Cost / Total Liters
     */
    public static BigDecimal calculateAveragePricePerLiter(BigDecimal totalCost, BigDecimal totalLiters) {
        if (totalLiters == null || totalLiters.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return totalCost.divide(totalLiters, SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate fuel consumption variance
     * Formula: (Actual - Expected) / Expected * 100
     */
    public static BigDecimal calculateVariancePercentage(BigDecimal actual, BigDecimal expected) {
        if (expected == null || expected.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return actual.subtract(expected)
                .divide(expected, SCALE, ROUNDING_MODE)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculate absolute fuel consumption variance in liters
     * Formula: Actual - Expected
     */
    public static BigDecimal calculateVariance(BigDecimal actual, BigDecimal expected) {
        if (expected == null) {
            return BigDecimal.ZERO;
        }
        return actual.subtract(expected).abs();
    }

    /**
     * Calculate expected fuel consumption based on distance and efficiency
     * Formula: Distance / Efficiency
     */
    public static BigDecimal calculateExpectedConsumption(BigDecimal distance, BigDecimal fuelEfficiency) {
        if (fuelEfficiency == null || fuelEfficiency.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return distance.divide(fuelEfficiency, SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate fuel efficiency in km/liter
     * Formula: Total Kilometers / Total Liters
     */
    public static BigDecimal calculateFuelEfficiency(BigDecimal totalKm, BigDecimal totalLiters) {
        if (totalLiters == null || totalLiters.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return totalKm.divide(totalLiters, SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate average fuel consumption from list of values
     * Formula: Sum of all values / Count
     */
    public static BigDecimal calculateAverage(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate standard deviation for fuel consumption values
     */
    public static BigDecimal calculateStandardDeviation(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal average = calculateAverage(values);
        BigDecimal sumOfSquares = values.stream()
                .map(value -> value.subtract(average).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal variance = sumOfSquares.divide(BigDecimal.valueOf(values.size()), SCALE, ROUNDING_MODE);
        return variance.sqrt(new java.math.MathContext(SCALE));
    }

    /**
     * Detect if consumption is anomalous based on threshold
     * Returns true if variance exceeds threshold percentage
     */
    public static boolean isAnomalous(BigDecimal actual, BigDecimal expected, BigDecimal thresholdPercent) {
        if (expected == null || expected.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        BigDecimal variance = calculateVariancePercentage(actual, expected);
        return variance.abs().compareTo(thresholdPercent) > 0;
    }

    /**
     * Calculate potential fuel theft amount
     * Returns the excess consumption in liters
     */
    public static BigDecimal calculateTheftAmount(BigDecimal actual, BigDecimal expected) {
        if (actual.compareTo(expected) <= 0) {
            return BigDecimal.ZERO;
        }
        return actual.subtract(expected);
    }

    /**
     * Calculate cost of potential fuel theft
     */
    public static BigDecimal calculateTheftCost(BigDecimal theftAmount, BigDecimal pricePerLiter) {
        return theftAmount.multiply(pricePerLiter).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate monthly fuel budget alert threshold (typically 80%)
     */
    public static BigDecimal calculateBudgetAlertThreshold(BigDecimal monthlyBudget) {
        return monthlyBudget.multiply(BigDecimal.valueOf(0.8)).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate remaining fuel budget
     */
    public static BigDecimal calculateRemainingBudget(BigDecimal monthlyBudget, BigDecimal spent) {
        BigDecimal remaining = monthlyBudget.subtract(spent);
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    /**
     * Calculate budget utilization percentage
     * Formula: (Spent / Budget) * 100
     */
    public static BigDecimal calculateBudgetUtilization(BigDecimal spent, BigDecimal budget) {
        if (budget == null || budget.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return spent.divide(budget, SCALE, ROUNDING_MODE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Calculate fuel consumption trend (positive = increasing, negative = decreasing)
     * Formula: ((Current - Previous) / Previous) * 100
     */
    public static BigDecimal calculateConsumptionTrend(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, SCALE, ROUNDING_MODE)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculate fleet average fuel consumption
     */
    public static BigDecimal calculateFleetAverage(List<BigDecimal> vehicleConsumptions) {
        return calculateAverage(vehicleConsumptions);
    }

    /**
     * Check if consumption spike exists (increase > threshold)
     */
    public static boolean isConsumptionSpike(BigDecimal current, BigDecimal previous, BigDecimal spikeThreshold) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        BigDecimal increase = current.subtract(previous)
                .divide(previous, SCALE, ROUNDING_MODE);
        return increase.compareTo(spikeThreshold) > 0;
    }

    /**
     * Calculate price per liter change percentage
     */
    public static BigDecimal calculatePriceChange(BigDecimal currentPrice, BigDecimal previousPrice) {
        if (previousPrice == null || previousPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return currentPrice.subtract(previousPrice)
                .divide(previousPrice, SCALE, ROUNDING_MODE)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Check if price fluctuation exists (change > threshold)
     */
    public static boolean isPriceFluctuation(BigDecimal currentPrice, BigDecimal averagePrice, BigDecimal fluctuationThreshold) {
        if (averagePrice == null || averagePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        BigDecimal change = currentPrice.subtract(averagePrice).abs()
                .divide(averagePrice, SCALE, ROUNDING_MODE)
                .multiply(BigDecimal.valueOf(100));
        return change.compareTo(fluctuationThreshold) > 0;
    }

    /**
     * Calculate cumulative fuel cost for period
     */
    public static BigDecimal calculateCumulativeCost(List<BigDecimal> costs) {
        return costs.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate average monthly fuel cost
     */
    public static BigDecimal calculateAverageMonthlyCost(BigDecimal totalCost, int monthCount) {
        if (monthCount <= 0) {
            return BigDecimal.ZERO;
        }
        return totalCost.divide(BigDecimal.valueOf(monthCount), SCALE, ROUNDING_MODE);
    }

    /**
     * Check if budget overage exists
     */
    public static boolean isBudgetOverage(BigDecimal spent, BigDecimal budget) {
        return spent.compareTo(budget) > 0;
    }

    /**
     * Calculate budget overage amount
     */
    public static BigDecimal calculateBudgetOverage(BigDecimal spent, BigDecimal budget) {
        if (spent.compareTo(budget) <= 0) {
            return BigDecimal.ZERO;
        }
        return spent.subtract(budget).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Round BigDecimal to standard scale
     */
    public static BigDecimal round(BigDecimal value) {
        return value.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate potential savings percentage based on best-performing vehicle
     */
    public static BigDecimal calculateSavingsPotential(BigDecimal currentAvg, BigDecimal bestPerformance) {
        if (bestPerformance == null || bestPerformance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal difference = currentAvg.subtract(bestPerformance);
        return difference.divide(currentAvg, SCALE, ROUNDING_MODE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);
    }
}
