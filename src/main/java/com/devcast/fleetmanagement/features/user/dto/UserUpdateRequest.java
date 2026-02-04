package com.devcast.fleetmanagement.features.user.dto;

import com.devcast.fleetmanagement.features.user.model.util.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user update requests
 * Client cannot provide: id, status, timestamps (server managed)
 * All fields are optional for partial updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @Size(min = 2, max = 255, message = "Full name must be between 2 and 255 characters")
    private String fullName;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Pattern(regexp = "^[+]?[(]?[0-9]{3}[)]?[-\\s.]?[0-9]{3}[-\\s.]?[0-9]{4,6}$|^$",
            message = "Phone number must be valid or empty")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    private Role role;

    /**
     * Optional password change (only if provided)
     * Must meet same requirements as creation
     */
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String newPassword;

    /**
     * Validation: Password requirements when password is provided
     */
    @AssertTrue(message = "Password must contain uppercase, lowercase, digit, and special character")
    public boolean isPasswordValid() {
        if (newPassword == null || newPassword.isEmpty()) {
            return true; // Optional field, only validate if provided
        }
        return newPassword.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }
}
