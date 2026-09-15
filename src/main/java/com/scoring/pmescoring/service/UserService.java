package com.scoring.pmescoring.service;

import com.scoring.pmescoring.common.service.CrudService;
import com.scoring.pmescoring.dto.request.user.UpdateUserRequest;
import com.scoring.pmescoring.dto.request.user.UserLogin;
import com.scoring.pmescoring.dto.request.user.UserRequest;
import com.scoring.pmescoring.dto.response.user.DataTokenResponse;
import com.scoring.pmescoring.dto.response.user.UserResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService extends CrudService<Long, UserRequest, UpdateUserRequest, UserResponse> {

    void validToken(String token);
    DataTokenResponse login(UserLogin login);
}
