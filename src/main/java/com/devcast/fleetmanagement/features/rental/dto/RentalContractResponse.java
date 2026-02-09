package com.devcast.fleetmanagement.features.rental.dto;

import com.devcast.fleetmanagement.features.rental.model.RentalContract;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Rental Contract Response DTO
 * Complete view of a rental contract with all details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalContractResponse {

    private Long id;
    private Long companyId;
    private Long clientId;
    private String clientName;
    private String contractNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean includeDriver;
    private RentalContract.PricingModel pricingModel;
    private RentalContract.RentalStatus status;
    private BigDecimal totalCost;
    private BigDecimal paidAmount;
    private BigDecimal balanceDue;
    private String notes;
    private List<RentalVehicleResponse> rentalVehicles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RentalVehicleResponse {
        private Long id;
        private Long vehicleId;
        private String vehiclePlateNumber;
        private String vehicleType;
        private Long driverId;
        private String driverName;
        private BigDecimal agreedRate;
    }

    public static RentalContractResponse fromEntity(RentalContract contract) {
        return RentalContractResponse.builder()
                .id(contract.getId())
                .companyId(contract.getCompany().getId())
                .clientId(contract.getClient().getId())
                .clientName(contract.getClient().getName())
                .contractNumber(contract.getContractNumber())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .includeDriver(contract.getIncludeDriver())
                .pricingModel(contract.getPricingModel())
                .status(contract.getStatus())
                .notes("")
                .createdAt(contract.getCreatedAt())
                .build();
    }
}
