package com.devcast.fleetmanagement.features.auth.service;

import java.util.Optional;

/**
 * Authentication Service Interface
 * Handles user authentication, JWT token management, and session handling
 */
public interface AuthenticationService {

    // ==================== Authentication ====================

    /**
     * Authenticate user with email and password
     */
    AuthenticationResponse authenticate(String email, String password);

    /**
     * Validate token
     */
    boolean validateToken(String token);

    /**
     * Refresh access token
     */
    AuthenticationResponse refreshToken(String refreshToken);

    /**
     * Logout user
     */
    void logout(String token);

    /**
     * Check if token is valid
     */
    boolean isTokenValid(String token);

    /**
     * Get token expiration time
     */
    Optional<Long> getTokenExpiration(String token);

    // ==================== Account Management ====================

    /**
     * Register new user
     */
    void registerUser(RegistrationRequest request);

    /**
     * Verify email
     */
    void verifyEmail(String token);

    /**
     * Forgot password request
     */
    void requestPasswordReset(String email);

    /**
     * Reset password
     */
    void resetPassword(String token, String newPassword);

    /**
     * Verify account
     */
    void verifyAccount(String email, String verificationCode);

    // Data Transfer Objects

    record AuthenticationResponse(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            String tokenType,
            UserInfo userInfo
    ) {}

    record UserInfo(
            Long userId,
            String email,
            String fullName,
            String role,
            Long companyId
    ) {}

    record RegistrationRequest(
            String email,
            String password,
            String fullName,
            Long companyId
    ) {}
}
