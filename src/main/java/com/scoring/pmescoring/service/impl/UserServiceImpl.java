package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.exception.BusinessException;
import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.common.util.PageableSanitizer;
import com.scoring.pmescoring.config.security.TokenService;
import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.dto.request.user.UpdateUserRequest;
import com.scoring.pmescoring.dto.request.user.UserLogin;
import com.scoring.pmescoring.dto.request.user.UserRequest;
import com.scoring.pmescoring.dto.response.user.DataTokenResponse;
import com.scoring.pmescoring.dto.response.user.UserResponse;
import com.scoring.pmescoring.mapper.UserMapper;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.repository.UserRepository;
import com.scoring.pmescoring.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PageableSanitizer pageableSanitizer;
    private final AuthenticationManager manager;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse create(UserRequest userRequest) {
        log.info("Initiating user creation process for email: {}", userRequest.email());

        boolean existsEmail = userRepository.existsByEmailAndStatus(userRequest.email(), EntityStatus.ACTIVE);

        if (existsEmail) {
            log.warn("Business rule violation: Attempted to create user with an already existing email: {}", userRequest.email());
            throw new BusinessException("User already exists with this email.");
        }

        User user = userMapper.toEntity(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.password()));

        User savedUser = userRepository.save(user);

        log.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Initiating logical deletion for user ID: {}", id);

        User user = userRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Deletion failed. Active user not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with ID: " + id);
                });

        user.delete();
        userRepository.save(user);

        log.info("User ID: {} successfully marked as deleted", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        log.info("Fetching user with ID: {}", id);

        User user = userRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Fetch failed. Active user not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with ID: " + id);
                });

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        log.info("Fetching paginated active users");

        pageable = pageableSanitizer.sanitize(pageable);
        Page<User> usersPage = userRepository.findByStatus(EntityStatus.ACTIVE, pageable);

        return usersPage.map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UpdateUserRequest updateUserRequest) {
        log.info("Initiating update process for user ID: {}", id);

        User user = userRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Update failed. Active user not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with ID: " + id);
                });

        if (!user.getEmail().equals(updateUserRequest.email())) {
            boolean existsEmail = userRepository.existsByEmailAndStatus(updateUserRequest.email(), EntityStatus.ACTIVE);
            if (existsEmail) {
                log.warn("Business rule violation: Attempted to update user ID: {} with an already existing email: {}", id, updateUserRequest.email());
                throw new BusinessException("This email is already in use by another user.");
            }
        }

        user.updateData(updateUserRequest.email(), updateUserRequest.password(), updateUserRequest.type());
        user.setPassword(passwordEncoder.encode(updateUserRequest.password()));
        User updatedUser = userRepository.save(user);

        log.info("User ID: {} updated successfully", updatedUser.getId());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public void validToken(String token) {
        log.info("Processing token validation request at service layer");
        this.tokenService.getSubject(token);
    }

    @Override
    @Transactional
    public DataTokenResponse login(UserLogin login) {
        log.info("Attempting authentication for email: {}", login.email());

        var user = new UsernamePasswordAuthenticationToken(login.email(), login.password());
        var userAuth = manager.authenticate(user);

        log.info("Authentication successful for email: {}. Generating JWT token.", login.email());
        return new DataTokenResponse(tokenService.generateToken((User) userAuth.getPrincipal()));
    }
}