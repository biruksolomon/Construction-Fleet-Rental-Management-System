package com.devcast.fleetmanagement.features.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Standard API Response DTO
 * Wraps all API responses with consistent structure for success/error handling
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API Response wrapper")
public record ApiResponse<T>(
        @JsonProperty("success")
        @Schema(description = "Whether the request was successful", example = "true")
        boolean success,

        @JsonProperty("message")
        @Schema(description = "Response message", example = "Operation successful")
        String message,

        @JsonProperty("data")
        @Schema(description = "Response data payload")
        T data,

        @JsonProperty("timestamp")
        @Schema(description = "Response timestamp", example = "2026-02-10T21:00:00")
        LocalDateTime timestamp
) {
    /**
     * Create successful response with data
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    /**
     * Create successful response
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now());
    }

    /**
     * Create error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}
