package com.devcast.fleetmanagement.features.auth.service;

import com.devcast.fleetmanagement.features.auth.dto.*;
import com.devcast.fleetmanagement.features.auth.model.EmailVerificationCode;
import com.devcast.fleetmanagement.features.auth.model.PasswordResetCode;
import com.devcast.fleetmanagement.features.auth.repository.EmailVerificationCodeRepository;
import com.devcast.fleetmanagement.features.auth.repository.PasswordResetCodeRepository;
import com.devcast.fleetmanagement.features.company.model.Company;
import com.devcast.fleetmanagement.features.company.repository.CompanyRepository;
import com.devcast.fleetmanagement.features.user.model.User;
import com.devcast.fleetmanagement.features.user.model.util.Role;
import com.devcast.fleetmanagement.features.user.repository.UserRepository;
import com.devcast.fleetmanagement.security.jwt.JwtAuthenticationException;
import com.devcast.fleetmanagement.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Authentication Service Implementation
 * Core business logic for user authentication, registration, and token management
 *
 * Multi-tenant aware: All operations are scoped to company context
 * Security-focused: Password hashing, JWT token lifecycle management, email verification, password reset
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Authenticate user with email and password
     * Business Logic:
     * 1. Find user by email
     * 2. Verify password
     * 3. Check user status (ACTIVE)
     * 4. Verify company is active
     * 5. Generate JWT tokens
     * 6. Return auth response with user info
     */
    @Override
    public AuthenticationResponse authenticate(String email, String password) {
        // Validate input
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new JwtAuthenticationException("Email and password are required");
        }

        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new JwtAuthenticationException("User not found with email: " + email));

        // Check user status
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new JwtAuthenticationException("User account is not active. Status: " + user.getStatus());
        }

        // Verify company is active
        Company company = user.getCompany();
        if (company.getStatus() != Company.CompanyStatus.ACTIVE) {
            throw new JwtAuthenticationException("Company account is suspended");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new JwtAuthenticationException("Invalid email or password");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                company.getId(),
                user.getEmail(),
                user.getRole().getAuthority()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(),
                company.getId(),
                user.getEmail()
        );

        // Create response
        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().getDisplayName(),
                company.getId()
        );

        return new AuthenticationResponse(
                accessToken,
                refreshToken,
                jwtTokenProvider.getTokenExpirationTime(accessToken),
                "Bearer",
                userInfo
        );
    }

    /**
     * Validate JWT token
     * Checks token signature, expiration, and integrity
     */
    @Override
    public boolean validateToken(String token) {
        try {
            jwtTokenProvider.validateToken(token);
            return true;
        } catch (JwtAuthenticationException e) {
            return false;
        }
    }

    /**
     * Refresh access token using refresh token
     * Business Logic:
     * 1. Validate refresh token
     * 2. Extract user info from token
     * 3. Verify user still exists and is active
     * 4. Verify company is active
     * 5. Generate new access token
     */
    @Override
    public AuthenticationResponse refreshToken(String refreshToken) {
        // Validate refresh token
        if (!validateToken(refreshToken)) {
            throw new JwtAuthenticationException("Invalid or expired refresh token");
        }

        // Extract user ID and company ID from token
        Long userId = jwtTokenProvider.getUserIdFromJwt(refreshToken);
        Long companyId = jwtTokenProvider.getCompanyIdFromJwt(refreshToken);

        // Find user and verify existence
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new JwtAuthenticationException("User not found"));

        // Verify user is still active
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new JwtAuthenticationException("User account is no longer active");
        }

        // Verify company is still active
        Company company = user.getCompany();
        if (company.getId().longValue() != companyId.longValue()) {
            throw new JwtAuthenticationException("Token company mismatch");
        }

        if (company.getStatus() != Company.CompanyStatus.ACTIVE) {
            throw new JwtAuthenticationException("Company account is suspended");
        }

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                company.getId(),
                user.getEmail(),
                user.getRole().getAuthority()
        );

        // Refresh token stays the same
        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().getDisplayName(),
                company.getId()
        );

        return new AuthenticationResponse(
                newAccessToken,
                refreshToken,
                jwtTokenProvider.getTokenExpirationTime(newAccessToken),
                "Bearer",
                userInfo
        );
    }

    /**
     * Logout user (token invalidation)
     * In JWT-based systems, logout is typically client-side (discard token)
     * This can be extended to implement token blacklist mechanism
     */
    @Override
    public void logout(String token) {
        // Implementation note: Token invalidation can be done via:
        // 1. Redis blacklist (store revoked tokens)
        // 2. Database token store
        // 3. Client-side token discard (current approach - stateless)

        // For now, just validate token exists
        validateToken(token);
    }

    /**
     * Check if token is valid (for middleware/filters)
     */
    @Override
    public boolean isTokenValid(String token) {
        return validateToken(token);
    }

    /**
     * Get token expiration time in milliseconds
     */
    @Override
    public Optional<Long> getTokenExpiration(String token) {
        try {
            long expirationTime = jwtTokenProvider.getTokenExpirationTime(token);
            return Optional.of(expirationTime);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Register new user with email verification
     * Business Logic:
     * 1. Validate registration request
     * 2. Check email is not already registered
     * 3. Verify company exists and is active
     * 4. Hash password using BCrypt
     * 5. Create user with INACTIVE status (until email verified)
     * 6. Generate and send verification code
     *
     * RBAC Rules:
     * - ADMIN or OWNER can register users for their company
     * - Users can self-register only if no admin exists (first user)
     * - Role is always set to DRIVER on registration (can be changed by ADMIN/OWNER)
     */
    @Override
    public void registerUser(RegistrationRequest request) {
        log.info("Registering new user with email: {}", request.email());

        // Validate input
        if (request.email() == null || request.email().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.password() == null || request.password().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (request.fullName() == null || request.fullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (request.companyId() == null || request.companyId() <= 0) {
            throw new IllegalArgumentException("Valid company ID is required");
        }

        // Check email is not already registered
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Verify company exists and is active
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (company.getStatus() != Company.CompanyStatus.ACTIVE) {
            throw new IllegalArgumentException("Company is not active. Registration denied.");
        }

        // RBAC: Check if user has permission to create user
        try {
            com.devcast.fleetmanagement.security.util.SecurityUtils.getCurrentUser();
            // User is authenticated - check permissions
            if (!com.devcast.fleetmanagement.security.util.SecurityUtils.hasPermission(
                    com.devcast.fleetmanagement.features.user.model.util.Permission.CREATE_USER)) {
                throw new IllegalArgumentException("You do not have permission to register users");
            }
            // Verify multi-tenant access
            if (!com.devcast.fleetmanagement.security.util.SecurityUtils.canAccessCompany(request.companyId())) {
                throw new IllegalArgumentException("You can only register users in your own company");
            }
        } catch (IllegalStateException e) {
            // User not authenticated - allow self-registration only if first user
            long companyUserCount = userRepository.countByCompanyId(request.companyId());
            if (companyUserCount > 0) {
                throw new IllegalArgumentException("Registration is disabled. Please contact your administrator.");
            }
        }

        // Hash password
        String passwordHash = passwordEncoder.encode(request.password());

        // Create new user with INACTIVE status until email verification
        User user = User.builder()
                .company(company)
                .email(request.email())
                .fullName(request.fullName())
                .passwordHash(passwordHash)
                .role(Role.DRIVER)
                .status(User.UserStatus.INACTIVE)  // INACTIVE until email verified
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered with ID: {}", savedUser.getId());

        // Generate and send verification code
        String verificationCode = generateVerificationCode();
        EmailVerificationCode emailCode = EmailVerificationCode.builder()
                .email(request.email())
                .code(verificationCode)
                .build();
        emailVerificationCodeRepository.save(emailCode);

        // Send verification email
        emailService.sendVerificationCode(request.email(), verificationCode);
        log.info("Verification code sent to: {}", request.email());
    }

    /**
     * Verify email using verification code
     * Business Logic:
     * 1. Find verification code record
     * 2. Validate code is not expired
     * 3. Validate code has not been used
     * 4. Find user by email
     * 5. Activate user (set to ACTIVE)
     * 6. Mark verification code as verified
     */
    @Override
    public void verifyEmail(String email, String code) {
        log.info("Verifying email: {} with code", email);

        // Find verification code
        EmailVerificationCode verification = emailVerificationCodeRepository
                .findByEmailAndCode(email, code)
                .orElseThrow(() -> {
                    log.warn("Invalid verification code for email: {}", email);
                    return new IllegalArgumentException("Invalid verification code");
                });

        // Check if expired
        if (verification.isExpired()) {
            log.warn("Verification code expired for email: {}", email);
            throw new IllegalArgumentException("Verification code has expired. Please request a new one.");
        }

        // Check if already verified
        if (verification.isVerified()) {
            log.warn("Verification code already used for email: {}", email);
            throw new IllegalArgumentException("This code has already been used");
        }

        // Find and activate user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for email: {}", email);
                    return new IllegalArgumentException("User not found");
                });

        // Activate user
        user.setStatus(User.UserStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Mark verification code as verified
        verification.setVerified(true);
        emailVerificationCodeRepository.save(verification);

        // Send welcome email
        emailService.sendAccountActivatedEmail(email, user.getFullName());
        log.info("User email verified and activated: {}", email);
    }

    /**
     * Resend verification code to email
     */
    public void resendVerificationCode(String email) {
        log.info("Resending verification code to: {}", email);

        // Check user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check user not already verified
        if (user.getStatus() == User.UserStatus.ACTIVE) {
            throw new IllegalArgumentException("User email is already verified");
        }

        // Delete old verification code
        emailVerificationCodeRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                .ifPresent(emailVerificationCodeRepository::delete);

        // Generate new verification code
        String verificationCode = generateVerificationCode();
        EmailVerificationCode emailCode = EmailVerificationCode.builder()
                .email(email)
                .code(verificationCode)
                .build();
        emailVerificationCodeRepository.save(emailCode);

        // Send verification email
        emailService.sendVerificationCode(email, verificationCode);
        log.info("Verification code resent to: {}", email);
    }

    /**
     * Generate 6-digit verification code
     */
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * Request password reset
     * Business Logic:
     * 1. Find user by email
     * 2. Generate unique reset code (UUID-based, 32 chars)
     * 3. Save reset code with 24-hour expiration
     * 4. Send password reset email
     */
    @Override
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Password reset requested for non-existent email: {}", email);
                    return new IllegalArgumentException("If user exists, password reset email sent");
                });

        // Delete any existing reset codes
        passwordResetCodeRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                .ifPresent(passwordResetCodeRepository::delete);

        // Generate reset code (UUID-based for security)
        String resetCode = UUID.randomUUID().toString();
        PasswordResetCode resetCodeEntity = PasswordResetCode.builder()
                .email(email)
                .code(resetCode)
                .expiryTime(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
        passwordResetCodeRepository.save(resetCodeEntity);

        // Send password reset email
        emailService.sendPasswordResetEmail(email, user.getFullName(), resetCode);
        log.info("Password reset code sent to: {}", email);
    }

    /**
     * Reset password using reset code
     * Business Logic:
     * 1. Find reset code
     * 2. Validate code (not expired, not used)
     * 3. Find user
     * 4. Hash and update password
     * 5. Mark reset code as used
     */
    @Override
    public void resetPassword(String email, String code, String newPassword) {
        log.info("Resetting password for email: {}", email);

        // Validate new password
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        // Find reset code
        PasswordResetCode resetCode = passwordResetCodeRepository
                .findByEmailAndCode(email, code)
                .orElseThrow(() -> {
                    log.warn("Invalid reset code for email: {}", email);
                    return new IllegalArgumentException("Invalid reset code");
                });

        // Check if code is valid
        if (!resetCode.isValid()) {
            if (resetCode.isExpired()) {
                log.warn("Reset code expired for email: {}", email);
                throw new IllegalArgumentException("Password reset code has expired. Please request a new one.");
            }
            if (resetCode.getUsed()) {
                log.warn("Reset code already used for email: {}", email);
                throw new IllegalArgumentException("This reset code has already been used");
            }
        }

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update password
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(hashedPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Mark reset code as used
        resetCode.setUsed(true);
        passwordResetCodeRepository.save(resetCode);

        // Send confirmation email
        emailService.sendPasswordChangedEmail(email, user.getFullName());
        log.info("Password reset successfully for: {}", email);
    }

    /**
     * Resend password reset code
     * Business Logic:
     * 1. Find user
     * 2. Check user status
     * 3. Delete old reset code
     * 4. Generate and send new reset code
     */
    @Override
    public void resendPasswordResetCode(String email) {
        log.info("Resending password reset code to: {}", email);

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Delete old reset code if exists
        passwordResetCodeRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                .ifPresent(passwordResetCodeRepository::delete);

        // Generate new reset code
        String resetCode = UUID.randomUUID().toString();
        PasswordResetCode resetCodeEntity = PasswordResetCode.builder()
                .email(email)
                .code(resetCode)
                .expiryTime(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
        passwordResetCodeRepository.save(resetCodeEntity);

        // Send password reset email
        emailService.sendPasswordResetEmail(email, user.getFullName(), resetCode);
        log.info("Password reset code resent to: {}", email);
    }

    /**
     * Verify password reset code validity
     * Business Logic:
     * 1. Find reset code
     * 2. Check expiration and usage status
     * 3. Return validity
     */
    @Override
    public boolean isPasswordResetCodeValid(String email, String code) {
        return passwordResetCodeRepository
                .findByEmailAndCode(email, code)
                .map(PasswordResetCode::isValid)
                .orElse(false);
    }
}
