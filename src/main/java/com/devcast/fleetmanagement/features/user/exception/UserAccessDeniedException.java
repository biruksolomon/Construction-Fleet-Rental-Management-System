package com.devcast.fleetmanagement.features.user.exception;

/**
 * Exception thrown when user access is denied due to RBAC or multi-tenant restrictions
 */
public class UserAccessDeniedException extends RuntimeException {

    private Long userId;
    private Long requestingUserId;
    private String reason;

    public UserAccessDeniedException(Long userId, String reason) {
        super("Access denied for user " + userId + ": " + reason);
        this.userId = userId;
        this.reason = reason;
    }

    public UserAccessDeniedException(Long userId, Long requestingUserId, String reason) {
        super("User " + requestingUserId + " cannot access user " + userId + ": " + reason);
        this.userId = userId;
        this.requestingUserId = requestingUserId;
        this.reason = reason;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRequestingUserId() {
        return requestingUserId;
    }

    public String getReason() {
        return reason;
    }
}
