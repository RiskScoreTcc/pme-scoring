package com.scoring.pmescoring.dto.response.user;

import com.scoring.pmescoring.model.TypeUser;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representation of a system user profile and account details")
public record UserResponse(

        @Schema(description = "Unique internal identifier of the user", example = "1")
        Long id,

        @Schema(description = "Email address used as the username for authentication", example = "analyst@pmescoring.com")
        String email,

        @Schema(description = "Access profile type defining the user's role and security permissions", example = "CREDIT_ANALYST")
        TypeUser type
) {
}