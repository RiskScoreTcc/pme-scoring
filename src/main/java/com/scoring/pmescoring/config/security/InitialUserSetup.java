package com.scoring.pmescoring.config.security;

import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.TypeUser;
import com.scoring.pmescoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitialUserSetup implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${api.admin.email}")
    private String adminEmail;

    @Value("${api.admin.password:#{null}}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing security setup: Checking default admin user configuration");

        if (!StringUtils.hasText(adminPassword)) {
            log.error("Bootstrap failed: CRITICAL SECURITY ERROR. ADMIN_PASSWORD environment variable is not set.");
            throw new IllegalStateException("CRITICAL SECURITY ERROR: ADMIN_PASSWORD environment variable is not set!");
        }

        boolean adminExists = userRepository.existsByEmailAndStatus(adminEmail, EntityStatus.ACTIVE);

        if (!adminExists) {
            log.info("Default admin user not found. Creating secure initial admin account for email: {}", adminEmail);
            User admin = new User(adminEmail, passwordEncoder.encode(adminPassword), TypeUser.ADMIN);
            userRepository.save(admin);
            log.info("Secure initial admin user created successfully");
        } else {
            log.info("Default admin user already exists. Setup skipped.");
        }
    }
}