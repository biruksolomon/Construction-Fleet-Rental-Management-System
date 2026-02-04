package com.devcast.fleetmanagement.features.company.dto;

public record CompanySubscriptionInfo(
        String subscriptionType,
        String status,
        Long expiryDate,
        Long maxVehicles,
        Long maxUsers,
        Double monthlyCost
) {}
