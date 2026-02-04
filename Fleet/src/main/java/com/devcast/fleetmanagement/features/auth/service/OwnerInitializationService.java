package com.devcast.fleetmanagement.features.auth.service;

import com.devcast.fleetmanagement.features.company.model.Company;
import com.devcast.fleetmanagement.features.company.repository.CompanyRepository;
import com.devcast.fleetmanagement.features.user.model.User;
import com.devcast.fleetmanagement.features.user.model.util.Role;
import com.devcast.fleetmanagement.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Owner Initialization Service
 *
 * Automatically creates a default OWNER user and company on application startup.
 * This ensures the system is always accessible to at least one administrator.
 *
 * Hardcoded Owner Credentials (First Time Setup):
 * - Email: owner@fleetmanagement.com
 * - Password: Owner@123456
 * - Company: Fleet Management System
 * - Role: OWNER
 *
 * IMPORTANT: Change these credentials immediately in production!
 *
 * Security Note:
 * - This is only executed on first run (checks if owner exists)
 * - The owner has full system access (80+ permissions)
 * - After initial setup, use admin panel to create additional users
 * - All operations are RBAC-protected
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerInitializationService implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Hardcoded initial owner credentials
    private static final String DEFAULT_OWNER_EMAIL = "owner@fleetmanagement.com";
    private static final String DEFAULT_OWNER_PASSWORD = "Owner@123456";
    private static final String DEFAULT_OWNER_NAME = "System Owner";
    private static final String DEFAULT_COMPANY_NAME = "Fleet Management System";
    private static final String DEFAULT_COMPANY_EMAIL = "admin@fleetmanagement.com";

    /**
     * Initialize owner on application startup
     * This CommandLineRunner is executed after Spring Boot starts
     */
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            // Check if owner already exists
            if (userRepository.findByEmail(DEFAULT_OWNER_EMAIL).isPresent()) {
                log.info("Default owner already exists. Skipping initialization.");
                return;
            }

            log.info("Initializing default OWNER user and company...");

            // Create default company
            Company company = createDefaultCompany();
            log.info("Created default company: {} (ID: {})", company.getName(), company.getId());

            // Create default owner user
            User owner = createDefaultOwner(company);
            log.info("Created default OWNER user: {} (ID: {})", owner.getEmail(), owner.getId());

            log.info("===============================================");
            log.info("IMPORTANT: Default Owner Credentials");
            log.info("===============================================");
            log.info("Email: {}", DEFAULT_OWNER_EMAIL);
            log.info("Password: {}", DEFAULT_OWNER_PASSWORD);
            log.info("Company: {}", DEFAULT_COMPANY_NAME);
            log.info("Role: OWNER (Full System Access)");
            log.info("===============================================");
            log.info("SECURITY WARNING: Change these credentials immediately!");
            log.info("This is only for initial setup. Use admin panel to create");
            log.info("additional users with proper authentication.");
            log.info("===============================================");

        } catch (Exception e) {
            log.error("Failed to initialize default owner", e);
            throw e;
        }
    }

    /**
     * Create default company
     */
    private Company createDefaultCompany() {
        Company company = Company.builder()
                .name(DEFAULT_COMPANY_NAME)
                .businessType(Company.BusinessType.MIXED)
                .currency("ETB")
                .timezone("UTC")
                .language("en")
                .status(Company.CompanyStatus.ACTIVE)
                .build();

        return companyRepository.save(company);
    }


    /**
     * Create default owner user
     */
    private User createDefaultOwner(Company company) {
        String passwordHash = passwordEncoder.encode(DEFAULT_OWNER_PASSWORD);

        User owner = User.builder()
                .company(company)
                .email(DEFAULT_OWNER_EMAIL)
                .fullName(DEFAULT_OWNER_NAME)
                .passwordHash(passwordHash)
                .role(Role.OWNER)
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(owner);
    }
}
