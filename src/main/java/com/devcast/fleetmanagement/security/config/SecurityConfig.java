package com.devcast.fleetmanagement.security.config;

import com.devcast.fleetmanagement.security.filter.JwtAuthenticationFilter;
import com.devcast.fleetmanagement.security.filter.PermissionAuthorizationFilter;
import com.devcast.fleetmanagement.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security Configuration
 * JWT-based, stateless authentication
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF (JWT = stateless)
                .csrf(csrf -> csrf.disable())

                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless session management
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Exception handling
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                        .accessDeniedHandler(new JwtAccessDeniedHandler())
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // -------- SWAGGER / OPENAPI (PUBLIC) --------
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // -------- PUBLIC ENDPOINTS --------
                        .requestMatchers(HttpMethod.POST,
                                "/auth/register",
                                "/auth/login",
                                "/auth/refresh-token",
                                "/auth/verify-email",
                                "/auth/resend-verification",
                                "/auth/request-password-reset",
                                "/auth/confirm-password-reset",
                                "/auth/resend-password-reset"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/auth/validate-reset-code"
                        ).permitAll()

                        .requestMatchers(
                                "/public/**",
                                "/health",
                                "/info"
                        ).permitAll()

                        // -------- OWNER --------
                        .requestMatchers(HttpMethod.POST, "/companies").hasRole("OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/companies/**").hasRole("OWNER")

                        // -------- ADMIN & OWNER --------
                        .requestMatchers(
                                "/users/**",
                                "/company-settings/**",
                                "/audit-logs/**"
                        ).hasAnyRole("OWNER", "ADMIN")

                        // -------- FLEET MANAGER --------
                        .requestMatchers(
                                "/vehicles/**",
                                "/drivers/**",
                                "/rentals/**",
                                "/v1/vehicles/**",
                                "/v1/rentals/**"
                        ).hasAnyRole("OWNER", "ADMIN", "FLEET_MANAGER")

                        // -------- ACCOUNTANT --------
                        .requestMatchers(
                                "/invoices/**",
                                "/payroll/**"
                        ).hasAnyRole("OWNER", "ADMIN", "ACCOUNTANT")

                        // -------- DRIVER --------
                        .requestMatchers("/fuel-logs/**")
                        .hasAnyRole("OWNER", "ADMIN", "FLEET_MANAGER", "DRIVER")

                        .requestMatchers("/gps-logs/**")
                        .hasAnyRole("OWNER", "ADMIN", "FLEET_MANAGER")

                        // -------- EVERYTHING ELSE --------
                        .anyRequest().authenticated()
                );

        // JWT authentication filter
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
        );

        // Permission-based authorization filter
        http.addFilterAfter(
                new PermissionAuthorizationFilter(),
                JwtAuthenticationFilter.class
        );

        return http.build();
    }

    /**
     * CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                List.of("http://localhost:3000", "http://localhost:3002", "http://localhost:8080")
        );
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        );
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
