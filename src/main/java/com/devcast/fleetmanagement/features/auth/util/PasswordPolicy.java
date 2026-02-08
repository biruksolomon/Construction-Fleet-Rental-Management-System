package com.devcast.fleetmanagement.features.auth.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Password Policy Configuration and Validation
 * Enforces strong password policies configurable via application.yml
 */
@Component
@Getter
public class PasswordPolicy {

    @Value("${password.policy.min-length:8}")
    private int minLength;

    @Value("${password.policy.max-length:128}")
    private int maxLength;

    @Value("${password.policy.require-uppercase:true}")
    private boolean requireUppercase;

    @Value("${password.policy.require-lowercase:true}")
    private boolean requireLowercase;

    @Value("${password.policy.require-numbers:true}")
    private boolean requireNumbers;

    @Value("${password.policy.require-special-chars:true}")
    private boolean requireSpecialChars;

    private static final String SPECIAL_CHARS_PATTERN = "[@$!%*?&]";
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile(SPECIAL_CHARS_PATTERN);

    /**
     * Validate password against policy
     * @param password password to validate
     * @return true if password meets policy requirements
     * @throws IllegalArgumentException if password does not meet requirements
     */
    public void validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        if (password.length() < minLength) {
            throw new IllegalArgumentException("Password must be at least " + minLength + " characters long");
        }

        if (password.length() > maxLength) {
            throw new IllegalArgumentException("Password must not exceed " + maxLength + " characters");
        }

        if (requireUppercase && !UPPERCASE_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }

        if (requireLowercase && !LOWERCASE_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }

        if (requireNumbers && !NUMBER_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one number");
        }

        if (requireSpecialChars && !SPECIAL_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one special character (@$!%*?&)");
        }
    }

    /**
     * Get password policy description
     */
    public String getPolicyDescription() {
        StringBuilder sb = new StringBuilder("Password policy: ");
        sb.append("Min ").append(minLength).append(" chars, Max ").append(maxLength).append(" chars");

        if (requireUppercase) sb.append(", requires uppercase");
        if (requireLowercase) sb.append(", requires lowercase");
        if (requireNumbers) sb.append(", requires numbers");
        if (requireSpecialChars) sb.append(", requires special chars (@$!%*?&)");

        return sb.toString();
    }
}
