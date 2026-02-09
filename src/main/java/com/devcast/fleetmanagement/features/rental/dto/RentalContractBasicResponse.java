package com.devcast.fleetmanagement.features.rental.dto;

import com.devcast.fleetmanagement.features.rental.model.RentalContract;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Rental Contract Basic Response DTO
 * Lightweight view for listing rental contracts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalContractBasicResponse {

    private Long id;
    private String contractNumber;
    private Long clientId;
    private String clientName;
    private LocalDate startDate;
    private LocalDate endDate;
    private RentalContract.RentalStatus status;
    private int vehicleCount;
    private BigDecimal totalCost;
    private BigDecimal balanceDue;

    public static RentalContractBasicResponse fromEntity(RentalContract contract) {
        return RentalContractBasicResponse.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .clientId(contract.getClient().getId())
                .clientName(contract.getClient().getName())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .status(contract.getStatus())
                .vehicleCount(contract.getRentalVehicles().size())
                .build();
    }
}
