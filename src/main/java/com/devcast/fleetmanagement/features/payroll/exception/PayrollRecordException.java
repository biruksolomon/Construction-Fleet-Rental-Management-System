package com.devcast.fleetmanagement.features.payroll.exception;

public class PayrollRecordException extends RuntimeException {
    
    public PayrollRecordException(String message) {
        super(message);
    }
    
    public PayrollRecordException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static PayrollRecordException notFound(Long recordId) {
        return new PayrollRecordException("Payroll record not found: " + recordId);
    }
    
    public static PayrollRecordException invalidStatus(String currentStatus, String targetStatus) {
        return new PayrollRecordException("Cannot transition from " + currentStatus + " to " + targetStatus);
    }
    
    public static PayrollRecordException alreadyProcessed(Long recordId) {
        return new PayrollRecordException("Payroll record already processed: " + recordId);
    }
}
