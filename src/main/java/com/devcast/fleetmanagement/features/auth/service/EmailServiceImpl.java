package com.devcast.fleetmanagement.features.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Email Service Implementation
 * Uses JavaMail to send verification emails and notifications
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@fleetmanagement.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8180}")
    private String baseUrl;

    /**
     * Send verification code email with HTML content
     */
    @Override
    public void sendVerificationCode(String email, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Fleet Management - Email Verification Code");

            String htmlContent = buildVerificationEmailHtml(verificationCode);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification code email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Send welcome email to newly registered user
     */
    @Override
    public void sendWelcomeEmail(String email, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Welcome to Fleet Management System");

            String htmlContent = buildWelcomeEmailHtml(fullName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Welcome email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    /**
     * Send password reset email with reset code
     */
    @Override
    public void sendPasswordResetEmail(String email, String fullName, String resetCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Fleet Management - Password Reset Request");

            String htmlContent = buildPasswordResetEmailHtml(fullName, resetCode);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Send password changed confirmation email
     */
    @Override
    public void sendPasswordChangedEmail(String email, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Fleet Management - Password Changed Successfully");

            String htmlContent = buildPasswordChangedEmailHtml(fullName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password changed confirmation email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send password changed email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send password changed email", e);
        }
    }

    /**
     * Send account activated email
     */
    @Override
    public void sendAccountActivatedEmail(String email, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Your Fleet Management Account is Verified");

            String htmlContent = buildAccountActivatedEmailHtml(fullName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Account activated email sent to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send account activated email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send account activated email", e);
        }
    }

    /**
     * Build HTML content for verification email
     */
    private String buildVerificationEmailHtml(String verificationCode) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #333;'>Email Verification Required</h2>" +
                "<p>Thank you for registering with Fleet Management System.</p>" +
                "<p>Your verification code is:</p>" +
                "<div style='background-color: #f0f0f0; padding: 15px; text-align: center; margin: 20px 0;'>" +
                "<h1 style='letter-spacing: 5px; color: #007bff;'>" + verificationCode + "</h1>" +
                "</div>" +
                "<p>This code will expire in 24 hours.</p>" +
                "<p>If you did not request this code, please ignore this email.</p>" +
                "<hr style='border: none; border-top: 1px solid #ddd;'>" +
                "<p style='color: #666; font-size: 12px;'>Fleet Management System</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Build HTML content for welcome email
     */
    private String buildWelcomeEmailHtml(String fullName) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #333;'>Welcome to Fleet Management System</h2>" +
                "<p>Hello " + fullName + ",</p>" +
                "<p>Your account has been successfully verified and activated.</p>" +
                "<p>You can now log in to the Fleet Management System and start managing your fleet operations.</p>" +
                "<p><strong>Quick Start:</strong></p>" +
                "<ul>" +
                "<li>Dashboard - View your fleet overview</li>" +
                "<li>Vehicles - Manage your vehicles</li>" +
                "<li>Drivers - Manage driver information</li>" +
                "<li>Rentals - Track and manage rental contracts</li>" +
                "</ul>" +
                "<p>If you have any questions, please contact our support team.</p>" +
                "<hr style='border: none; border-top: 1px solid #ddd;'>" +
                "<p style='color: #666; font-size: 12px;'>Fleet Management System</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Build HTML content for password reset email
     */
    private String buildPasswordResetEmailHtml(String fullName, String resetCode) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #333;'>Password Reset Request</h2>" +
                "<p>Hello " + fullName + ",</p>" +
                "<p>We received a request to reset your password for your Fleet Management account.</p>" +
                "<p>Use the following code to reset your password:</p>" +
                "<div style='background-color: #f0f0f0; padding: 15px; text-align: center; margin: 20px 0;'>" +
                "<h1 style='letter-spacing: 3px; color: #dc3545; font-family: monospace;'>" + resetCode + "</h1>" +
                "</div>" +
                "<p><strong>Important:</strong></p>" +
                "<ul>" +
                "<li>This code will expire in 24 hours</li>" +
                "<li>Do not share this code with anyone</li>" +
                "<li>If you did not request this reset, please ignore this email</li>" +
                "</ul>" +
                "<hr style='border: none; border-top: 1px solid #ddd;'>" +
                "<p style='color: #666; font-size: 12px;'>Fleet Management System</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Build HTML content for password changed confirmation email
     */
    private String buildPasswordChangedEmailHtml(String fullName) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #28a745;'>Password Changed Successfully</h2>" +
                "<p>Hello " + fullName + ",</p>" +
                "<p>Your password has been changed successfully.</p>" +
                "<p>If you did not make this change, please contact our support team immediately.</p>" +
                "<p><strong>Security Tips:</strong></p>" +
                "<ul>" +
                "<li>Use a strong, unique password</li>" +
                "<li>Never share your password with anyone</li>" +
                "<li>Update your password regularly</li>" +
                "<li>Log out from other sessions if you suspect unauthorized access</li>" +
                "</ul>" +
                "<hr style='border: none; border-top: 1px solid #ddd;'>" +
                "<p style='color: #666; font-size: 12px;'>Fleet Management System</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Build HTML content for account activated email
     */
    private String buildAccountActivatedEmailHtml(String fullName) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #333;'>Account Verified Successfully</h2>" +
                "<p>Hello " + fullName + ",</p>" +
                "<p>Your email has been verified successfully!</p>" +
                "<p>Your Fleet Management account is now fully activated and ready to use.</p>" +
                "<p>You can now access all features of the platform.</p>" +
                "<hr style='border: none; border-top: 1px solid #ddd;'>" +
                "<p style='color: #666; font-size: 12px;'>Fleet Management System</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
