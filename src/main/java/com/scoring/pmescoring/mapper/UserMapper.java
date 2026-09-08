package com.scoring.pmescoring.mapper;

import com.scoring.pmescoring.common.mapper.GenericMapper;
import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.dto.request.user.UpdateUserRequest;
import com.scoring.pmescoring.dto.request.user.UserRequest;
import com.scoring.pmescoring.dto.response.user.UserResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper implements GenericMapper<UserRequest, UserResponse, User> {
    @Override
    public User toEntity(UserRequest userRequest) {
        if (userRequest == null) {
            return null;
        }
        return new User(userRequest.email(), userRequest.password(), userRequest.type());
    }

    @Override
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(user.getId(), user.getEmail(), user.getUserType());
    }

}
