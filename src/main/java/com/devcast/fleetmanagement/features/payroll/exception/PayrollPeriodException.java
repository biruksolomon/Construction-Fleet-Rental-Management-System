package com.devcast.fleetmanagement.features.payroll.exception;

public class PayrollPeriodException extends RuntimeException {
    
    public PayrollPeriodException(String message) {
        super(message);
    }
    
    public PayrollPeriodException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static PayrollPeriodException notFound(Long periodId) {
        return new PayrollPeriodException("Payroll period not found: " + periodId);
    }
    
    public static PayrollPeriodException invalidStatus(String status) {
        return new PayrollPeriodException("Invalid payroll period status: " + status);
    }
}
