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
     * Generate JWT access token with role and permissions
     *
     * Token claims:
     * - userId: User ID
     * - companyId: Company ID (tenant)
     * - role: User's role authority (e.g., "ROLE_OWNER")
     * - permissions: List of permission codes (for frontend explicit access control)
     */
    public String generateAccessToken(Long userId, Long companyId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("companyId", companyId);
        claims.put("role", role);
        // Permissions list is added during authentication to make JWT more useful for frontend
        // Frontend can read permissions directly without additional DB calls

        return createToken(claims, email, jwtExpirationMs);
    }

    /**
     * Generate JWT access token with extended claims including permissions
     * Used during authentication flow
     */
    public String generateAccessToken(Long userId, Long companyId, String email, String role, java.util.List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("companyId", companyId);
        claims.put("role", role);
        claims.put("permissions", permissions);

        return createToken(claims, email, jwtExpirationMs);
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
     * Extract role from JWT token
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
