package com.scoring.pmescoring.dto.request.user;

import com.scoring.pmescoring.model.TypeUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for requesting an update to an existing system user account")
public record UpdateUserRequest(

        @Schema(description = "Updated email address used as the username for authentication", example = "new.analyst@pmescoring.com")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Updated account password (must contain at least 8 characters)", example = "SecurePass123!")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @Schema(description = "Updated access profile type defining the user's role and permissions", example = "ADMIN")
        TypeUser type
) {
}