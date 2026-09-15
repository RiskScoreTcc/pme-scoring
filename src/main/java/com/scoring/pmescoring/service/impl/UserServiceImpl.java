package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.exception.BusinessException;
import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.dto.request.user.UpdateUserRequest;
import com.scoring.pmescoring.dto.request.user.UserRequest;
import com.scoring.pmescoring.dto.response.user.UserResponse;
import com.scoring.pmescoring.mapper.UserMapper;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.repository.UserRepository;
import com.scoring.pmescoring.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponse create(UserRequest userRequest) {
        boolean existsEmail = userRepository.existsByEmailAndStatus(userRequest.email(), EntityStatus.ACTIVE);

        if (existsEmail) {
            throw new BusinessException("User already exists with this email.");
        }

        User user = userMapper.toEntity(userRequest);
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
        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }
}