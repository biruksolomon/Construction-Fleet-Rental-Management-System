package com.devcast.fleetmanagement.features.driver.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Driver Update Request DTO
 * Used for updating driver information with optional fields
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverUpdateRequest {
    
    @Size(min = 5, max = 50, message = "License number must be between 5 and 50 characters")
    private String licenseNumber;
    
    @FutureOrPresent(message = "License expiry date must be in future or present")
    private LocalDate licenseExpiry;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Hourly wage must be greater than 0")
    private BigDecimal hourlyWage;
    
    private String employmentType;
    private String status;
    private String insuranceNumber;
    private LocalDate insuranceExpiry;
    private String licenseType;
}
