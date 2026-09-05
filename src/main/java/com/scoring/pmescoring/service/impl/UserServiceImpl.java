package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.dto.request.user.UpdateUserRequest;
import com.scoring.pmescoring.dto.request.user.UserRequest;
import com.scoring.pmescoring.dto.response.user.UserResponse;
import com.scoring.pmescoring.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    @Override
    @Transactional
    public UserResponse create(UserRequest userRequest) {
        return null;
    }

    @Override
    @Transactional
    public void delete(Long aLong) {

    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long aLong) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return null;
    }

    @Override
    @Transactional
    public UserResponse update(Long aLong, UpdateUserRequest updateUserRequest) {
        return null;
    }
}
