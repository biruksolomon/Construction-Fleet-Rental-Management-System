package com.DevCast.Fleet_Management.security.config;

import com.DevCast.Fleet_Management.security.filter.JwtAuthenticationFilter;
import com.DevCast.Fleet_Management.security.filter.PermissionAuthorizationFilter;
import com.DevCast.Fleet_Management.security.jwt.JwtTokenProvider;
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

                        // -------- PUBLIC ENDPOINTS --------
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh-token"
                        ).permitAll()

                        .requestMatchers(
                                "/api/public/**",
                                "/health",
                                "/info"
                        ).permitAll()

                        // -------- OWNER --------
                        .requestMatchers(HttpMethod.POST, "/api/companies").hasRole("OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/companies/**").hasRole("OWNER")

                        // -------- ADMIN & OWNER --------
                        .requestMatchers(
                                "/api/users/**",
                                "/api/company-settings/**",
                                "/api/audit-logs/**"
                        ).hasAnyRole("OWNER", "ADMIN")

                        // -------- FLEET MANAGER --------
                        .requestMatchers(
                                "/api/vehicles/**",
                                "/api/drivers/**",
                                "/api/rentals/**"
                        ).hasAnyRole("OWNER", "ADMIN", "FLEET_MANAGER")

                        // -------- ACCOUNTANT --------
                        .requestMatchers(
                                "/api/invoices/**",
                                "/api/payroll/**"
                        ).hasAnyRole("OWNER", "ADMIN", "ACCOUNTANT")

                        // -------- DRIVER --------
                        .requestMatchers("/api/fuel-logs/**")
                        .hasAnyRole("OWNER", "ADMIN", "FLEET_MANAGER", "DRIVER")

                        .requestMatchers("/api/gps-logs/**")
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
                List.of("http://localhost:3000", "http://localhost:8080")
        );
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
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
