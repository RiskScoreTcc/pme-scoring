package com.scoring.pmescoring.dto.response.user;

import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.TypeUser;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Representation of a system user profile and account details")
public record UserResponse(

        @Schema(description = "Unique internal identifier of the user", example = "1")
        Long id,

        @Schema(description = "Email address used as the username for authentication", example = "analyst@pmescoring.com")
        String email,

        @Schema(description = "Access profile type defining the user's role and security permissions", example = "CREDIT_ANALYST")
        TypeUser type,

        @Schema(description = "Current account status", example = "ACTIVE")
        EntityStatus status,

        @Schema(description = "Date and time of the user's last successful login", example = "2026-10-02T22:45:30")
        LocalDateTime lastAccess,

        @Schema(description = "Date on which the user account was created", example = "2026-08-15")
        LocalDate creationDate
) {
}