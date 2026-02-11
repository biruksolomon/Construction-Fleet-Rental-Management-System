package com.devcast.fleetmanagement.features.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for resending verification code
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resend verification code request")
public class ResendVerificationRequest {

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Schema(description = "User email address", example = "user@example.com")
    private String email;
}
