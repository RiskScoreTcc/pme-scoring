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
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PageableSanitizer pageableSanitizer;
    private final AuthenticationManager manager;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PageableSanitizer pageableSanitizer, AuthenticationManager manager, TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.pageableSanitizer = pageableSanitizer;
        this.manager = manager;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse create(UserRequest userRequest) {
        boolean existsEmail = userRepository.existsByEmailAndStatus(userRequest.email(), EntityStatus.ACTIVE);

        if (existsEmail) {
            throw new BusinessException("User already exists with this email.");
        }

        User user = userMapper.toEntity(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.password()));

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.delete();
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        pageable = pageableSanitizer.sanitize(pageable);
        Page<User> usersPage = userRepository.findByStatus(EntityStatus.ACTIVE, pageable);
        return usersPage.map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (!user.getEmail().equals(updateUserRequest.email())) {
            boolean existsEmail = userRepository.existsByEmailAndStatus(updateUserRequest.email(), EntityStatus.ACTIVE);
            if (existsEmail) {
                throw new BusinessException("This email is already in use by another user.");
            }
        }

        user.updateData(updateUserRequest.email(), updateUserRequest.password(), updateUserRequest.type());
        user.setPassword(passwordEncoder.encode(updateUserRequest.password()));
        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public void validToken(String token) {
        this.tokenService.getSubject(token);
    }

    @Override
    @Transactional
    public DataTokenResponse login(UserLogin login) {
        var user = new UsernamePasswordAuthenticationToken(login.email(),login.password());
        var userAuth = manager.authenticate(user);
        return new DataTokenResponse(tokenService.generateToken((User)userAuth.getPrincipal()));
    }
}