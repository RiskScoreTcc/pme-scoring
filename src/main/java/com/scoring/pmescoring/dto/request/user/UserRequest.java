package com.scoring.pmescoring.dto.request.user;

import com.scoring.pmescoring.model.TypeUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for registering a new system user account with specific access profiles")
public record UserRequest(

        @Schema(description = "Email address for the new user, used as the unique username", example = "new.admin@pmescoring.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Initial password for the account (minimum 8 characters)", example = "SecurePass123!")
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @Schema(description = "Access profile type assigned to the user", example = "CREDIT_ANALYST")
        @NotNull(message = "User type is required")
        TypeUser type
) {
}