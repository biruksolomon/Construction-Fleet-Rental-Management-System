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
     * Generate JWT access token
     */
    public String generateAccessToken(Long userId, Long companyId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("companyId", companyId);
        claims.put("role", role);

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
