package com.devcast.fleetmanagement.features.auth.dto;

import java.time.LocalDateTime;

/**
 * Standard API Response DTO
 * Wraps all API responses with consistent structure for success/error handling
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
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
