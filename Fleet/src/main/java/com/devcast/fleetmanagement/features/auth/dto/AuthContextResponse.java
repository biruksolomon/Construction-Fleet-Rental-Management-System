package com.devcast.fleetmanagement.features.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Auth Context Response
 *
 * Single source of truth for frontend auth state.
 * Returned by GET /api/auth/context endpoint.
 *
 * Frontend uses this to initialize:
 * - User profile
 * - Company context
 * - Permissions & features
 * - Navigation & UI access control
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthContextResponse {

    @JsonProperty("user")
    private UserContext user;

    @JsonProperty("company")
    private CompanyContext company;

    @JsonProperty("roles")
    private List<String> roles;

    @JsonProperty("permissions")
    private List<String> permissions;

    @JsonProperty("features")
    private Map<String, Boolean> features;

    /**
     * User context within the auth response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserContext {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("email")
        private String email;

        @JsonProperty("fullName")
        private String fullName;
    }

    /**
     * Company context within the auth response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompanyContext {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("status")
        private String status;

        @JsonProperty("businessType")
        private String businessType;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("timezone")
        private String timezone;

        @JsonProperty("language")
        private String language;
    }
}
