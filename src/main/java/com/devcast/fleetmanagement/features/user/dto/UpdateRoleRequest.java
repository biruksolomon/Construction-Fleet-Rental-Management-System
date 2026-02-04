package com.devcast.fleetmanagement.features.user.dto;

import com.devcast.fleetmanagement.features.user.model.util.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for role assignment/update requests
 * Only role field, requires RBAC permission
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    private Role newRole;

    private String reason; // Optional audit reason for role change
}
