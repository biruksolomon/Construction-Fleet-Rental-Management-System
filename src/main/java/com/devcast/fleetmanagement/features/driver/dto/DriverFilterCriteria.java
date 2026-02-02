package com.devcast.fleetmanagement.features.driver.dto;

public  record DriverFilterCriteria(
        String status,
        String licenseType,
        Double minRating,
        Long fromDate,
        Long toDate
) {}
