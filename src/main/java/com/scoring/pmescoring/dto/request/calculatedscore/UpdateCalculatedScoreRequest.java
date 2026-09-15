package com.scoring.pmescoring.dto.request.calculatedscore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Payload for requesting an update to an existing calculated risk score")
public record UpdateCalculatedScoreRequest(

        @Schema(description = "Unique internal identifier of the firm associated with the score update", example = "1")
        @NotNull(message = "Firm ID is required")
        @Positive(message = "Firm ID must be greater than zero")
        Long firmId,

        @Schema(description = "Unique internal identifier of the user performing the update", example = "42")
        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be greater than zero")
        Long userId
) {
}