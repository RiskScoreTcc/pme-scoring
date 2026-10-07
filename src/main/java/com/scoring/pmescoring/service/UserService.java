package com.scoring.pmescoring.service;

import com.scoring.pmescoring.common.service.CrudService;
import com.scoring.pmescoring.common.service.ReadFilterService;
import com.scoring.pmescoring.common.service.ReadMetricsService;
import com.scoring.pmescoring.dto.request.user.UpdateUserRequest;
import com.scoring.pmescoring.dto.request.user.UserFilter;
import com.scoring.pmescoring.dto.request.user.UserLogin;
import com.scoring.pmescoring.dto.request.user.UserRequest;
import com.scoring.pmescoring.dto.response.user.DataTokenResponse;
import com.scoring.pmescoring.dto.response.user.UserMetricsResponse;
import com.scoring.pmescoring.dto.response.user.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService extends CrudService<Long, UserRequest, UpdateUserRequest, UserResponse>, ReadFilterService<UserResponse, UserFilter>, ReadMetricsService<UserMetricsResponse> {

    void validToken(String token);
    DataTokenResponse login(UserLogin login);
    void validateSearchFilter(UserFilter filter);
}
