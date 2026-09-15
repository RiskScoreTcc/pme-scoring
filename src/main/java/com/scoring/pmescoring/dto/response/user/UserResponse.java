package com.scoring.pmescoring.dto.response.user;

import com.scoring.pmescoring.model.TypeUser;

public record UserResponse(
        Long id,
        String email,
        TypeUser type
) {
}