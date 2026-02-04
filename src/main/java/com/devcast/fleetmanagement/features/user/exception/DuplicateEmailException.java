package com.devcast.fleetmanagement.features.user.exception;

/**
 * Exception thrown when attempting to create/update user with duplicate email
 */
public class DuplicateEmailException extends RuntimeException {

    private String email;
    private Long companyId;

    public DuplicateEmailException(String email, Long companyId) {
        super("Email already exists in company: " + email);
        this.email = email;
        this.companyId = companyId;
    }

    public DuplicateEmailException(String message) {
        super(message);
    }

    public String getEmail() {
        return email;
    }

    public Long getCompanyId() {
        return companyId;
    }
}
