package com.devcast.fleetmanagement.features.auth.service;

import com.devcast.fleetmanagement.features.auth.dto.*;

import java.util.Optional;

/**
 * IAuthenticationService Interface
 * Defines contract for authentication, registration, and password management
 * Implementations must handle multi-tenant isolation and RBAC
 */
public interface AuthenticationService {

    // ==================== Authentication ====================

    /**
     * Authenticate user with email and password
     * @param email User email
     * @param password User password
     * @return AuthenticationResponse with JWT tokens
     */
    AuthenticationResponse authenticate(String email, String password);

    /**
     * Validate JWT token
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    boolean validateToken(String token);

    /**
     * Refresh access token using refresh token
     * @param refreshToken Refresh token from previous login
     * @return New AuthenticationResponse with updated access token
     */
    AuthenticationResponse refreshToken(String refreshToken);

    /**
     * Logout user (token invalidation)
     * @param token JWT token to invalidate
     */
    void logout(String token);

    /**
     * Check if token is valid
     * @param token JWT token to check
     * @return true if valid, false otherwise
     */
    boolean isTokenValid(String token);

    /**
     * Get token expiration time
     * @param token JWT token
     * @return Token expiration time in milliseconds, or empty if invalid
     */
    Optional<Long> getTokenExpiration(String token);

    // ==================== Registration & Email Verification ====================

    /**
     * Register new user with email verification
     * User status set to INACTIVE until email verified
     * @param request RegistrationRequest with user details
     * @throws IllegalArgumentException if validation fails
     */
    void registerUser(RegistrationRequest request);

    /**
     * Verify email using verification code
     * Activates user account after email verification
     * @param email User email
     * @param code Verification code sent via email
     * @throws IllegalArgumentException if code is invalid or expired
     */
    void verifyEmail(String email, String code);

    /**
     * Resend verification code to email
     * Deletes old code and generates new one
     * @param email User email
     * @throws IllegalArgumentException if user not found or already verified
     */
    void resendVerificationCode(String email);

    // ==================== Password Management ====================

    /**
     * Request password reset
     * Generates reset code and sends via email
     * @param email User email
     * @throws IllegalArgumentException if user not found
     */
    void requestPasswordReset(String email);

    /**
     * Reset password using reset code
     * Validates code, updates password, and invalidates code
     * @param email User email
     * @param code Password reset code
     * @param newPassword New password (minimum 8 characters)
     * @throws IllegalArgumentException if code invalid, expired, or password too weak
     */
    void resetPassword(String email, String code, String newPassword);

    /**
     * Resend password reset code
     * Deletes old code and generates new one
     * @param email User email
     * @throws IllegalArgumentException if user not found
     */
    void resendPasswordResetCode(String email);

    /**
     * Verify password reset code validity
     * @param email User email
     * @param code Reset code to verify
     * @return true if code is valid, false otherwise
     */
    boolean isPasswordResetCodeValid(String email, String code);
}
