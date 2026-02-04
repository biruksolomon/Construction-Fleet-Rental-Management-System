package com.devcast.fleetmanagement.features.user.dto;

import com.devcast.fleetmanagement.features.user.model.util.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user creation requests
 * Client cannot provide: id, status, timestamps, passwordHash (server handles)
 * All fields required for new user creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 255, message = "Full name must be between 2 and 255 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[(]?[0-9]{3}[)]?[-\\s.]?[0-9]{3}[-\\s.]?[0-9]{4,6}$",
            message = "Phone number must be valid")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    /**
     * Validation: Password requirements
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     */
    @AssertTrue(message = "Password must contain uppercase, lowercase, digit, and special character")
    public boolean isPasswordValid() {
        if (password == null) return false;
        return password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }
}
