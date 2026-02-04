package com.devcast.fleetmanagement.features.user.dto;

import com.devcast.fleetmanagement.features.user.model.User;
import com.devcast.fleetmanagement.features.user.model.util.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal User response DTO for listings and references
 * Includes: id, name, email, role (only essential info)
 * Excludes: timestamps, phone, status (for list views)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBasicResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;

    /**
     * Convert User entity to UserBasicResponse DTO
     */
    public static UserBasicResponse fromEntity(User user) {
        return UserBasicResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    /**
     * Convert list of User entities to UserBasicResponse DTOs
     */
    public static List<UserBasicResponse> fromEntities(List<User> users) {
        List<UserBasicResponse> responses = new ArrayList<>();
        if (users != null) {
            users.forEach(user -> responses.add(fromEntity(user)));
        }
        return responses;
    }
}
