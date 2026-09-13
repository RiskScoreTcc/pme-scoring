package com.scoring.pmescoring.config.security;

import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.TypeUser;
import com.scoring.pmescoring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class InitialUserSetup implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${api.admin.email}")
    private String adminEmail;

    @Value("${api.admin.password:#{null}}")
    private String adminPassword;

    public InitialUserSetup(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        if (!StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException("CRITICAL SECURITY ERROR: ADMIN_PASSWORD environment variable is not set!");
        }

        boolean adminExists = userRepository.existsByEmailAndStatus(adminEmail, EntityStatus.ACTIVE);

        if (!adminExists) {
            User admin = new User(adminEmail, passwordEncoder.encode(adminPassword), TypeUser.ADMIN);
            userRepository.save(admin);
            System.out.println(">> Secure initial admin user created successfully.");
        }
    }
}