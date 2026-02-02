package com.devcast.fleetmanagement.features.audit.dto;

import java.util.List;

public  record DataIntegrityReport(
        Long companyId,
        boolean isIntegrityValid,
        List<String> anomalies,
        List<String> recommendations,
        Long reportDate
    ) {}