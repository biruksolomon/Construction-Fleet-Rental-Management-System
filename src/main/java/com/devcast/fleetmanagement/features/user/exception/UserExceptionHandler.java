package com.devcast.fleetmanagement.features.user.exception;

import com.devcast.fleetmanagement.features.auth.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for User-related exceptions
 * Provides consistent error responses across all user endpoints
 */
@RestControllerAdvice(basePackages = "com.devcast.fleetmanagement.features.user")
@Slf4j
public class UserExceptionHandler {

    /**
     * Handle UserNotFoundException
     * HTTP 404 Not Found
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found: " + ex.getMessage()));
    }

    /**
     * Handle DuplicateEmailException
     * HTTP 409 Conflict
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(DuplicateEmailException ex) {
        log.warn("Duplicate email: {} in company: {}", ex.getEmail(), ex.getCompanyId());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Email already exists: " + ex.getEmail()));
    }

    /**
     * Handle InvalidPasswordException
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPassword(InvalidPasswordException ex) {
        log.warn("Invalid password: {}", ex.getReason());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Password validation failed: " + ex.getReason()));
    }

    /**
     * Handle UserAccessDeniedException
     * HTTP 403 Forbidden
     */
    @ExceptionHandler(UserAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(UserAccessDeniedException ex) {
        log.warn("Access denied for user {}: {}", ex.getUserId(), ex.getReason());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied: " + ex.getReason()));
    }

    /**
     * Handle IllegalArgumentException (validation errors)
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation error: " + ex.getMessage()));
    }

    /**
     * Handle SecurityException (RBAC violations)
     * HTTP 403 Forbidden
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleSecurityException(SecurityException ex) {
        log.warn("Security exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Security error: " + ex.getMessage()));
    }

    /**
     * Handle MethodArgumentNotValidException (request body validation)
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        log.warn("Validation failed with {} errors", errors.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed"));
    }

    /**
     * Handle generic Exception
     * HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error: " + ex.getMessage()));
    }
}
