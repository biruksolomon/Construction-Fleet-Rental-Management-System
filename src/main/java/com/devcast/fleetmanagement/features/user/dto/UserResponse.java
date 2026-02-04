package com.devcast.fleetmanagement.features.user.dto;

import com.devcast.fleetmanagement.features.user.model.User;
import com.devcast.fleetmanagement.features.user.model.util.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Complete User response DTO with all user information
 * Excludes: passwordHash (never expose to client)
 * Includes: id, status, timestamps, all user details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private Long companyId;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private User.UserStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Convert User entity to UserResponse DTO
     * Never includes password hash
     */
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of User entities to UserResponse DTOs
     */
    public static List<UserResponse> fromEntities(List<User> users) {
        List<UserResponse> responses = new ArrayList<>();
        if (users != null) {
            users.forEach(user -> responses.add(fromEntity(user)));
        }
        return responses;
    }
}
