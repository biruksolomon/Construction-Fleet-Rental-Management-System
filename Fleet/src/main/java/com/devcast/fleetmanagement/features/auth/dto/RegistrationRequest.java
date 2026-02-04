package com.devcast.fleetmanagement.features.auth.dto;

public record RegistrationRequest(
        String email,
        String password,
        String fullName,
        Long companyId
) {}