package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Attempting to load active user details for email: {}", email);

        return userRepository.findByEmailAndStatus(email, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: User not found or inactive for email: {}", email);
                    return new UsernameNotFoundException("Invalid email or password");
                });
    }
}