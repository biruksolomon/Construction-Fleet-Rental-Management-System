package com.devcast.fleetmanagement.features.user.dto;

import com.devcast.fleetmanagement.features.user.model.util.Role;

public record UserFilterCriteria(
        Role role,
        String status,
        String department,
        Long fromDate,
        Long toDate
) {}