package com.devcast.fleetmanagement.features.user.exception;

/**
 * Exception thrown when password validation fails or password is incorrect
 */
public class InvalidPasswordException extends RuntimeException {

    private String reason;
    private Long userId;

    public InvalidPasswordException(String reason) {
        super("Password validation failed: " + reason);
        this.reason = reason;
    }

    public InvalidPasswordException(Long userId, String reason) {
        super("Password validation failed for user " + userId + ": " + reason);
        this.userId = userId;
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public Long getUserId() {
        return userId;
    }
}
