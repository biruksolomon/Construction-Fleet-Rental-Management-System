package com.devcast.fleetmanagement.features.user.dto;

import com.devcast.fleetmanagement.features.user.model.util.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Filter Criteria DTO
 * Used for filtering users by role, status, department, and date range
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFilterCriteria {
    private Role role;
    private String status;
    private String department;
    private Long fromDate;
    private Long toDate;
}
