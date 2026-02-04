package com.devcast.fleetmanagement.features.company.dto;

public record CompanyStatistics(
        Long totalVehicles,
        Long totalDrivers,
        Long totalClients,
        Long activeRentals,
        Double totalRevenue,
        Double pendingAmount
) {}
