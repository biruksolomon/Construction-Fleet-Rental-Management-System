package com.devcast.fleetmanagement.features.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for email verification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Email verification request")
public class VerifyEmailRequest {

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @NotBlank(message = "Verification code is required")
    @Schema(description = "Verification code sent to email", example = "123456")
    private String code;
}
