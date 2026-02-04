package com.devcast.fleetmanagement.features.company.dto;

import com.devcast.fleetmanagement.validation.Phone;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Client Creation/Update Request
 *
 * Handles client information submission.
 * Does not include auto-managed fields like id, timestamps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRequest {

    @NotBlank(message = "Client name is required")
    @Size(min = 2, max = 255, message = "Client name must be between 2 and 255 characters")
    private String name;

    @Phone
    private String phone;

    @Email(message = "Email must be valid")
    private String email;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
}
