package com.devcast.fleetmanagement.features.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Confirm Password Reset Request DTO
 * Used to confirm and execute password reset with verification code
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Confirm password reset request with verification code")
public class ConfirmPasswordResetRequest {

        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        @Schema(description = "User email address", example = "user@example.com")
        private String email;

        @NotBlank(message = "Reset code is required")
        @Schema(description = "Password reset verification code", example = "123456")
        private String code;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Schema(description = "New password", example = "newPassword123")
        private String newPassword;

        @NotBlank(message = "Password confirmation is required")
        @Schema(description = "Confirm new password", example = "newPassword123")
        private String confirmPassword;
}
