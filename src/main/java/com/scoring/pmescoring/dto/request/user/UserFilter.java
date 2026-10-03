package com.scoring.pmescoring.dto.request.user;

import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.TypeUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Schema(description = "Optional filters used to search and narrow down system users")
public record UserFilter(

        @Schema(
                description = "Unique internal identifier of the user. Must be a positive number",
                example = "1"
        )
        @Positive(message = "User ID must be greater than zero")
        Long id,

        @Schema(
                description = "User email address used to filter accounts by email",
                example = "analyst@pmescoring.com"
        )
        @Email(message = "Invalid email format")
        String email,

        @Schema(
                description = "Access profile type defining the user's role and security permissions",
                example = "CREDIT_ANALYST"
        )
        TypeUser type,

        @Schema(
                description = "Current account status used to filter users by their account status",
                example = "ACTIVE"
        )
        EntityStatus status,

        @Schema(
                description = "Start date for filtering users by their last successful access",
                example = "2026-09-01"
        )
        LocalDate lastAccessFrom,

        @Schema(
                description = "End date for filtering users by their last successful access",
                example = "2026-10-02"
        )
        LocalDate lastAccessTo,

        @Schema(
                description = "Start date for filtering users by account creation date",
                example = "2026-01-01"
        )
        LocalDate creationDateFrom,

        @Schema(
                description = "End date for filtering users by account creation date",
                example = "2026-10-02"
        )
        LocalDate creationDateTo
) {
}

