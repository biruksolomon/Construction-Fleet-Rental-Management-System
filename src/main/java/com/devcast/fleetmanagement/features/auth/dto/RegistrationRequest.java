package com.devcast.fleetmanagement.features.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registration request with user details")
public class RegistrationRequest {

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "User password", example = "password123")
    private String password;

    @NotBlank(message = "Full name is required")
    @Schema(description = "User full name", example = "John Doe")
    private String fullName;

    @NotNull(message = "Company ID is required")
    @Schema(description = "Company ID", example = "1")
    private Long companyId;
}
