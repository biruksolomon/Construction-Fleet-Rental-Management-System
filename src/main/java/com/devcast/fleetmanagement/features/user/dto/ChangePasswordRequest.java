package com.devcast.fleetmanagement.features.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for password change requests
 * Requires: current password for verification, new password with confirmation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 128, message = "New password must be between 8 and 128 characters")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    /**
     * Validation: New password must match confirmation
     */
    @AssertTrue(message = "New password and confirmation must match")
    public boolean isPasswordConfirmationValid() {
        if (newPassword == null || confirmPassword == null) {
            return false;
        }
        return newPassword.equals(confirmPassword);
    }

    /**
     * Validation: New password must meet security requirements
     */
    @AssertTrue(message = "New password must contain uppercase, lowercase, digit, and special character")
    public boolean isPasswordSecure() {
        if (newPassword == null) return false;
        return newPassword.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }

    /**
     * Validation: New password must differ from current password
     */
    @AssertTrue(message = "New password must be different from current password")
    public boolean isNewPasswordDifferent() {
        if (currentPassword == null || newPassword == null) {
            return true; // Let other validations handle null checks
        }
        return !currentPassword.equals(newPassword);
    }
}
