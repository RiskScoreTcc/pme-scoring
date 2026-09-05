package com.scoring.pmescoring.dto.request.user;

import com.scoring.pmescoring.model.TypeUser;

public record UpdateUserRequest(
        String email,
        String password,
        TypeUser type
) {
}
