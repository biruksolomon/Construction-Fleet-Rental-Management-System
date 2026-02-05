package com.devcast.fleetmanagement.features.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Document Verification Status DTO
 * Tracks verification status of driver documents: license, insurance, medical certificate, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVerificationStatus {
    private Long driverId;
    private String licenseStatus;
    private String insuranceStatus;
    private String backgroundCheckStatus;
    private Long lastVerificationDate;
    private boolean allDocumentsValid;
}
