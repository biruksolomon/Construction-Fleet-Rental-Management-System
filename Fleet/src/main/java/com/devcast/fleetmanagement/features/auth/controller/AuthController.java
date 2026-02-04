package com.devcast.fleetmanagement.features.auth.controller;

import com.devcast.fleetmanagement.features.auth.dto.*;
import com.devcast.fleetmanagement.features.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST Controller
 *
 * Endpoints:
 * - POST /api/auth/login - User login
 * - POST /api/auth/register - User registration
 * - POST /api/auth/refresh-token - Refresh access token
 * - POST /api/auth/logout - User logout
 * - POST /api/auth/request-password-reset - Initiate password reset
 * - GET /api/auth/me - Get current user info
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Login endpoint
     * POST /api/auth/login
     *
     * Request body: LoginRequest { email, password }
     * Response: AuthenticationResponse { accessToken, refreshToken, userInfo }
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Login with email and password")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        try {
            AuthenticationResponse response = authenticationService.authenticate(
                    request.email(),
                    request.password()
            );
            return ResponseEntity.ok(
                    ApiResponse.success(response, "Login successful")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Register endpoint with email verification
     * POST /api/auth/register
     *
     * Request body: RegistrationRequest { email, password, fullName, companyId }
     * Response: ApiResponse with message to verify email
     *
     * Next step: User must verify email using /api/auth/verify-email endpoint
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create new user account with email verification")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Registration successful, check email for verification code"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or email already registered"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found")
    })
    public ResponseEntity<ApiResponse<?>> register(
            @Valid @RequestBody RegistrationRequest request
    ) {
        try {
            authenticationService.registerUser(request);
            return ResponseEntity.ok(
                    ApiResponse.success("Registration successful. Check your email for verification code.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    /**
     * Verify email endpoint
     * POST /api/auth/verify-email
     *
     * Request body: VerifyEmailRequest { email, code }
     * Response: ApiResponse with success message
     *
     * After verification, user can login
     */
    @PostMapping("/verify-email")
    @Operation(summary = "Verify user email", description = "Verify email using verification code sent via email")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired code"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<?>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        try {
            authenticationService.verifyEmail(request.email(), request.code());
            return ResponseEntity.ok(
                    ApiResponse.success("Email verified successfully. You can now login.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Email verification failed: " + e.getMessage()));
        }
    }

    /**
     * Resend verification code endpoint
     * POST /api/auth/resend-verification
     *
     * Request body: ResendVerificationRequest { email }
     * Response: ApiResponse with success message
     */
    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification code", description = "Resend verification code to user email")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verification code resent"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid email or user already verified"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<?>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request
    ) {
        try {
            authenticationService.resendVerificationCode(request.email());
            return ResponseEntity.ok(
                    ApiResponse.success("Verification code sent to your email.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Resend failed: " + e.getMessage()));
        }
    }

    /**
     * Refresh token endpoint
     * POST /api/auth/refresh-token
     *
     * Request body: RefreshTokenRequest { refreshToken }
     * Response: AuthenticationResponse with new accessToken
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        try {
            AuthenticationResponse response = authenticationService.refreshToken(request.refreshToken());
            return ResponseEntity.ok(
                    ApiResponse.success(response, "Token refreshed successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token refresh failed: " + e.getMessage()));
        }
    }

    /**
     * Logout endpoint
     * POST /api/auth/logout
     *
     * Requires: Authorization header with Bearer token
     * Response: ApiResponse with success message
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidate current session")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String bearerToken
    ) {
        try {
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                String token = bearerToken.substring(7);
                authenticationService.logout(token);
            }
            // Clear security context
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(
                    ApiResponse.success("Logout successful")
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    ApiResponse.success("Logout successful")
            );
        }
    }

    /**
     * Get current user endpoint
     * GET /api/auth/me
     *
     * Requires: Valid JWT token in Authorization header
     * Response: UserInfo of authenticated user
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get authenticated user information")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User info retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Not authenticated"));
            }

            // Extract user details from authentication object
            Object details = auth.getDetails();
            if (details instanceof com.devcast.fleetmanagement.security.filter.JwtAuthenticationFilter.JwtUserDetails) {
                var userDetails = (com.devcast.fleetmanagement.security.filter.JwtAuthenticationFilter.JwtUserDetails) details;
                UserInfo userInfo = new UserInfo(
                        userDetails.getUserId(),
                        userDetails.getEmail(),
                        userDetails.getEmail(),  // Full name not available in JWT details
                        userDetails.getRole(),
                        userDetails.getCompanyId()
                );
                return ResponseEntity.ok(
                        ApiResponse.success(userInfo, "User information retrieved")
                );
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid authentication details"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Failed to retrieve user: " + e.getMessage()));
        }
    }

    /**
     * Request password reset endpoint
     * POST /api/auth/request-password-reset
     *
     * Request body: PasswordResetRequest { email }
     * Response: ApiResponse with success message
     *
     * Next step: User receives reset code via email, then calls /api/auth/confirm-password-reset
     */
    @PostMapping("/request-password-reset")
    @Operation(summary = "Request password reset", description = "Initiate password reset process")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reset code sent to email"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<ApiResponse<?>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        try {
            authenticationService.requestPasswordReset(request.email());
            return ResponseEntity.ok(
                    ApiResponse.success("If an account exists with that email, password reset instructions will be sent")
            );
        } catch (Exception e) {
            // Return generic message for security (don't reveal if email exists)
            return ResponseEntity.ok(
                    ApiResponse.success("If an account exists with that email, password reset instructions will be sent")
            );
        }
    }

    /**
     * Confirm password reset endpoint
     * POST /api/auth/confirm-password-reset
     *
     * Request body: ConfirmPasswordResetRequest { email, code, newPassword, confirmPassword }
     * Response: ApiResponse with success message
     *
     * Prerequisites:
     * - User received reset code via email from /api/auth/request-password-reset
     * - Both newPassword and confirmPassword must match
     * - Password must be at least 8 characters
     */
    @PostMapping("/confirm-password-reset")
    @Operation(summary = "Confirm password reset", description = "Complete password reset with verification code")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid code, expired code, or passwords don't match"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<?>> confirmPasswordReset(
            @Valid @RequestBody ConfirmPasswordResetRequest request
    ) {
        try {
            // Validate passwords match
            if (!request.newPassword().equals(request.confirmPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Passwords do not match"));
            }

            authenticationService.resetPassword(request.email(), request.code(), request.newPassword());
            return ResponseEntity.ok(
                    ApiResponse.success("Password reset successfully. You can now login with your new password.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Password reset failed: " + e.getMessage()));
        }
    }

    /**
     * Resend password reset code endpoint
     * POST /api/auth/resend-password-reset
     *
     * Request body: PasswordResetRequest { email }
     * Response: ApiResponse with success message
     */
    @PostMapping("/resend-password-reset")
    @Operation(summary = "Resend password reset code", description = "Resend password reset code if expired")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reset code resent"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<?>> resendPasswordResetCode(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        try {
            authenticationService.resendPasswordResetCode(request.email());
            return ResponseEntity.ok(
                    ApiResponse.success("Password reset code sent to your email.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Resend failed: " + e.getMessage()));
        }
    }

    /**
     * Validate password reset code endpoint
     * GET /api/auth/validate-reset-code?email={email}&code={code}
     *
     * Response: ApiResponse with boolean indicating code validity
     */
    @GetMapping("/validate-reset-code")
    @Operation(summary = "Validate reset code", description = "Check if password reset code is valid and not expired")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Code validity returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing parameters")
    })
    public ResponseEntity<ApiResponse<Boolean>> validateResetCode(
            @RequestParam String email,
            @RequestParam String code
    ) {
        try {
            boolean isValid = authenticationService.isPasswordResetCodeValid(email, code);
            return ResponseEntity.ok(
                    ApiResponse.success(isValid, isValid ? "Reset code is valid" : "Reset code is invalid or expired")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error validating code: " + e.getMessage()));
        }
    }

    /**
     * Validate token endpoint
     * POST /api/auth/validate-token
     *
     * Request body: RefreshTokenRequest { token }
     * Response: ApiResponse with boolean indicating token validity
     */
    @PostMapping("/validate-token")
    @Operation(summary = "Validate token", description = "Check if provided token is valid")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token validation result returned")
    })
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestBody RefreshTokenRequest request
    ) {
        boolean isValid = authenticationService.validateToken(request.refreshToken());
        return ResponseEntity.ok(
                ApiResponse.success(isValid, isValid ? "Token is valid" : "Token is invalid or expired")
        );
    }

    /**
     * Get complete auth context endpoint
     * GET /api/auth/context
     *
     * Requires: Valid JWT token in Authorization header
     * Response: Complete auth context with user info, company, permissions, and features
     */
    @GetMapping("/context")
    @Operation(summary = "Get auth context", description = "Get complete authentication context including user, company, permissions, and features")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Auth context retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<?>> getAuthContext() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Not authenticated"));
            }

            // Extract user details from authentication object
            Object details = auth.getDetails();
            if (details instanceof com.devcast.fleetmanagement.security.filter.JwtAuthenticationFilter.JwtUserDetails) {
                var userDetails = (com.devcast.fleetmanagement.security.filter.JwtAuthenticationFilter.JwtUserDetails) details;

                // Build auth context with user, company, permissions, and features
                var authContext = java.util.Map.of(
                        "user", java.util.Map.of(
                                "id", userDetails.getUserId(),
                                "email", userDetails.getEmail(),
                                "fullName", userDetails.getEmail()
                        ),
                        "company", java.util.Map.of(
                                "id", userDetails.getCompanyId(),
                                "name", "Company Name",
                                "status", "ACTIVE",
                                "businessType", "Transport",
                                "currency", "USD",
                                "timezone", "UTC",
                                "language", "en"
                        ),
                        "roles", java.util.List.of(userDetails.getRole()),
                        "permissions", authenticationService.getUserPermissions(userDetails.getUserId()),
                        "features", java.util.Map.of(
                                "INVOICING", true,
                                "PAYROLL", true,
                                "FUEL_TRACKING", true,
                                "GPS_TRACKING", true
                        )
                );

                return ResponseEntity.ok(
                        ApiResponse.success(authContext, "Auth context retrieved")
                );
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid authentication details"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Failed to retrieve auth context: " + e.getMessage()));
        }
    }
}
