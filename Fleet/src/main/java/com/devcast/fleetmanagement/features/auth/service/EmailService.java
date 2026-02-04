package com.devcast.fleetmanagement.features.auth.service;

/**
 * Email Service Interface
 * Handles sending verification emails and notifications
 */
public interface EmailService {

    /**
     * Send verification code email (6-digit code)
     * @param email Recipient email
     * @param verificationCode 6-digit verification code
     */
    void sendVerificationCode(String email, String verificationCode);

    /**
     * Send account activated email
     * @param email Recipient email
     * @param fullName User full name
     */
    void sendAccountActivatedEmail(String email, String fullName);

    void sendWelcomeEmail(String email, String fullName);

    /**
     * Send password reset email with reset code
     * @param email Recipient email
     * @param fullName User full name
     * @param resetCode Password reset code (UUID)
     */
    void sendPasswordResetEmail(String email, String fullName, String resetCode);

    /**
     * Send password changed confirmation email
     * @param email Recipient email
     * @param fullName User full name
     */
    void sendPasswordChangedEmail(String email, String fullName);
}
