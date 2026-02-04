package com.devcast.fleetmanagement.security.jwt;

import org.springframework.security.core.AuthenticationException;

/**
 * Custom exception for JWT authentication failures
 */
public class JwtAuthenticationException extends AuthenticationException {
    public JwtAuthenticationException(String msg) {
        super(msg);
    }

    public JwtAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
