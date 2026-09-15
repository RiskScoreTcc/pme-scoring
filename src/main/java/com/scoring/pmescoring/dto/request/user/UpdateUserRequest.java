package com.scoring.pmescoring.dto.request.user;

import com.scoring.pmescoring.model.TypeUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Email(message = "Invalid email format")
        String email,

        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        TypeUser type
) {
}