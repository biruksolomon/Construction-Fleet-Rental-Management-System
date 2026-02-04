package com.devcast.fleetmanagement.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token Provider for generating and validating JWT tokens
 * Handles authentication token lifecycle
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:your-secret-key-must-be-at-least-256-bits-long-for-HS256}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private int jwtExpirationMs;

    @Value("${jwt.refresh-expiration:604800000}")
    private int jwtRefreshExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate JWT access token with role, roles list, and permissions
     *
     * Token claims:
     * - userId: User ID
     * - companyId: Company ID (tenant)
     * - email: User email
     * - role: User's single role authority (e.g., "ROLE_OWNER") [DEPRECATED - use roles]
     * - roles: List of role authorities (currently single element, future-proof for multi-role)
     * - permissions: List of permission codes (for frontend explicit access control)
     *
     * Frontend reads roles and permissions directly from JWT.
     * No AOP or tenant resolution needed - everything is explicit in the token.
     */
    public String generateAccessToken(Long userId, Long companyId, String email, String role, java.util.List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("companyId", companyId);
        claims.put("email", email);
        claims.put("role", role); // Keep for backward compatibility
        claims.put("roles", java.util.Collections.singletonList(role)); // Explicit roles list
        claims.put("permissions", permissions != null ? permissions : new java.util.ArrayList<>());

        return createToken(claims, email, jwtExpirationMs);
    }

    /**
     * Legacy method - use generateAccessToken(userId, companyId, email, role, permissions) instead
     * @deprecated Use generateAccessToken with permissions parameter
     */
    @Deprecated
    public String generateAccessToken(Long userId, Long companyId, String email, String role) {
        return generateAccessToken(userId, companyId, email, role, new java.util.ArrayList<>());
    }

    /**
     * Generate JWT refresh token
     */
    public String generateRefreshToken(Long userId, Long companyId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("companyId", companyId);
        claims.put("type", "REFRESH");

        return createToken(claims, email, jwtRefreshExpirationMs);
    }

    /**
     * Create JWT token
     */
    private String createToken(Map<String, Object> claims, String subject, int expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extract email from JWT token
     */
    public String getEmailFromJwt(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Extract user ID from JWT token
     */
    public Long getUserIdFromJwt(String token) {
        return ((Number) Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId")).longValue();
    }

    /**
     * Extract company ID from JWT token
     */
    public Long getCompanyIdFromJwt(String token) {
        return ((Number) Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("companyId")).longValue();
    }

    /**
     * Extract role from JWT token (single role, primary authority)
     */
    public String getRoleFromJwt(String token) {
        return (String) Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role");
    }

    /**
     * Extract roles list from JWT token
     * Returns empty list if roles not present
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getRolesFromJwt(String token) {
        try {
            Object rolesObj = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("roles");

            if (rolesObj instanceof java.util.List) {
                return (java.util.List<String>) rolesObj;
            }
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Extract permissions list from JWT token
     * Returns empty list if permissions not present
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getPermissionsFromJwt(String token) {
        try {
            Object permissionsObj = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("permissions");

            if (permissionsObj instanceof java.util.List) {
                return (java.util.List<String>) permissionsObj;
            }
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            throw new JwtAuthenticationException("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            throw new JwtAuthenticationException("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException("JWT claims string is empty");
        }
    }

    /**
     * Get token expiration time
     */
    public long getTokenExpirationTime(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * Check if token is about to expire (within 5 minutes)
     */
    public boolean isTokenAboutToExpire(String token) {
        long expirationTime = getTokenExpirationTime(token);
        return expirationTime < (5 * 60 * 1000); // 5 minutes
    }
}
