package com.devcast.fleetmanagement.features.user.exception;

/**
 * Exception thrown when a user is not found
 */
public class UserNotFoundException extends RuntimeException {

    private Long userId;
    private String email;

    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
        this.userId = userId;
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long userId, Throwable cause) {
        super("User not found with ID: " + userId, cause);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
