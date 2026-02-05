package com.devcast.fleetmanagement.features.driver.exception;

/**
 * Exception thrown when driver work hours limit is exceeded
 */
public class WorkLimitExceededException extends RuntimeException {
    public WorkLimitExceededException(String message) {
        super(message);
    }

    public WorkLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    public static WorkLimitExceededException dailyLimit(Long driverId, Long hours) {
        return new WorkLimitExceededException("Driver " + driverId + " has exceeded daily work limit by " + hours + " hours");
    }

    public static WorkLimitExceededException weeklyLimit(Long driverId, Long hours) {
        return new WorkLimitExceededException("Driver " + driverId + " has exceeded weekly work limit by " + hours + " hours");
    }
}
