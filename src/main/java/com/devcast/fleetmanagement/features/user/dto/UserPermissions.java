package com.devcast.fleetmanagement.features.user.dto;

import com.devcast.fleetmanagement.features.user.model.util.Role;

import java.util.List;

public record UserPermissions(
        Long userId,
        String userName,
        Role role,
        List<String> permissions,
        List<String> modules
) {}