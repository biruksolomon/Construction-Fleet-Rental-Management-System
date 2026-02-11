package com.devcast.fleetmanagement.security.filter;

import com.devcast.fleetmanagement.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT Authentication Filter
 * Intercepts HTTP requests and validates JWT tokens
 * Extracts user information from tokens and sets SecurityContext
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Endpoints that should NOT be filtered by JWT
     */
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/auth/**",
            "/public/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/health",
            "/info"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = getJwtFromRequest(request);

            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

                String email = jwtTokenProvider.getEmailFromJwt(token);
                String role = jwtTokenProvider.getRoleFromJwt(token);
                Long userId = jwtTokenProvider.getUserIdFromJwt(token);
                Long companyId = jwtTokenProvider.getCompanyIdFromJwt(token);

                // IMPORTANT: Spring expects ROLE_ prefix for hasRole()
                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority("ROLE_" + role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.singleton(authority)
                        );

                authentication.setDetails(
                        new JwtUserDetails(userId, companyId, email, role)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

                logger.debug(
                        "Authenticated user={}, companyId={}, role={}",
                        email, companyId, role
                );
            }

        } catch (Exception ex) {
            logger.error("JWT authentication failed: {}", ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Skip JWT filter for public endpoints
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return EXCLUDED_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken)
                && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Holder for JWT user details
     */
    public static class JwtUserDetails {

        private final Long userId;
        private final Long companyId;
        private final String email;
        private final String role;

        public JwtUserDetails(
                Long userId,
                Long companyId,
                String email,
                String role
        ) {
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
