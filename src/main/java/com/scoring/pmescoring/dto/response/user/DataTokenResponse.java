package com.scoring.pmescoring.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response payload containing the generated JWT Bearer token after successful authentication")
public record DataTokenResponse(

        @Schema(description = "Encoded JWT token string used for authenticating subsequent restricted HTTP requests", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBwbWVzY29yaW5nLmNvbSIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxNzAwMDAzNjAwfQ.example_signature_token_string")
        String token
) {
}