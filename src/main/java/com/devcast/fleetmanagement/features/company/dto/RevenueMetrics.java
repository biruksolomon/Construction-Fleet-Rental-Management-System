package com.devcast.fleetmanagement.features.company.dto;

public record RevenueMetrics(
        Double totalIncome,
        Double totalExpenses,
        Double netProfit,
        Long transactionCount
) {}
