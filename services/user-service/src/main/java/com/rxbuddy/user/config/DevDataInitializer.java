package com.rxbuddy.user.config;

import com.rxbuddy.user.entity.Role;
import com.rxbuddy.user.entity.User;
import com.rxbuddy.user.entity.UserTenant;
import com.rxbuddy.user.repository.RoleRepository;
import com.rxbuddy.user.repository.UserRepository;
import com.rxbuddy.user.repository.UserTenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserTenantRepository userTenantRepository;

    @Bean
    public CommandLineRunner initDevData() {
        return args -> {
            log.info("Initializing dev data...");

            // Create Super Admin role
            Role superAdminRole = roleRepository.save(Role.builder()
                    .name("Super Admin")
                    .code("SUPER_ADMIN")
                    .description("Platform-level administrator with full access")
                    .isSystemRole(true)
                    .isActive(true)
                    .build());

            // Create Admin role
            Role adminRole = roleRepository.save(Role.builder()
                    .name("Admin")
                    .code("ADMIN")
                    .description("Tenant administrator with full tenant access")
                    .isSystemRole(true)
                    .isActive(true)
                    .build());

            // Create other roles
            roleRepository.save(Role.builder()
                    .name("Pharmacist")
                    .code("PHARMACIST")
                    .description("Inventory and billing management")
                    .isSystemRole(true)
                    .isActive(true)
                    .build());

            roleRepository.save(Role.builder()
                    .name("Salesman")
                    .code("SALESMAN")
                    .description("Billing and customer management")
                    .isSystemRole(true)
                    .isActive(true)
                    .build());

            // Create Super Admin user with password: Admin@123
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
            String passwordHash = encoder.encode("Admin@123");

            User superAdmin = userRepository.save(User.builder()
                    .name("Super Admin")
                    .phone("9999999999")
                    .email("admin@rxbuddy.com")
                    .passwordHash(passwordHash)
                    .isActive(true)
                    .build());

            // Link user to platform
            userTenantRepository.save(UserTenant.builder()
                    .user(superAdmin)
                    .tenantId(0L)
                    .tenantName("RXBuddy Platform")
                    .role(superAdminRole)
                    .isActive(true)
                    .build());

            log.info("Dev data initialized successfully!");
            log.info("Login credentials:");
            log.info("  Phone: 9999999999");
            log.info("  Password: Admin@123");
        };
    }
}
