package com.devcast.fleetmanagement.features.driver.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Driver Create Request DTO
 * Input for creating new driver records with validation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverCreateRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "License number is required")
    @Size(min = 5, max = 50, message = "License number must be between 5 and 50 characters")
    private String licenseNumber;
    
    @NotNull(message = "License expiry date is required")
    @FutureOrPresent(message = "License expiry date must be in future or present")
    private LocalDate licenseExpiry;
    
    @NotNull(message = "Hourly wage is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Hourly wage must be greater than 0")
    private BigDecimal hourlyWage;
    
    @NotNull(message = "Employment type is required")
    private String employmentType;
    
    private String insuranceNumber;
    private LocalDate insuranceExpiry;
    private String licenseType;
}
