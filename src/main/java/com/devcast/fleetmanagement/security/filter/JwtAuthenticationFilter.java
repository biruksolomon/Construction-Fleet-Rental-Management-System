package com.devcast.fleetmanagement.security.filter;

import com.devcast.fleetmanagement.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWT Authentication Filter
 * Intercepts HTTP requests and validates JWT tokens
 * Extracts user information from tokens and sets SecurityContext
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getJwtFromRequest(request);

            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromJwt(token);
                String role = jwtTokenProvider.getRoleFromJwt(token);
                Long userId = jwtTokenProvider.getUserIdFromJwt(token);
                Long companyId = jwtTokenProvider.getCompanyIdFromJwt(token);

                // Create authentication token with role as authority
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email, null, Collections.singleton(authority));

                // Store additional info in details
                authentication.setDetails(
                        new JwtUserDetails(userId, companyId, email, role));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Set Security Context with user: {}, company: {}, role: {}",
                        email, companyId, role);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Inner class to hold JWT user details
     */
    public static class JwtUserDetails {
        private final Long userId;
        private final Long companyId;
        private final String email;
        private final String role;

        public JwtUserDetails(Long userId, Long companyId, String email, String role) {
            this.userId = userId;
            this.companyId = companyId;
            this.email = email;
            this.role = role;
        }

        public Long getUserId() {
            return userId;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }
    }
}
