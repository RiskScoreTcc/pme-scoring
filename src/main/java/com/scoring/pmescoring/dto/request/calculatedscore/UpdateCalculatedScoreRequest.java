package com.scoring.pmescoring.dto.request.calculatedscore;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCalculatedScoreRequest(
        @NotNull(message = "Firm ID is required")
        @Positive(message = "Firm ID must be greater than zero")
        Long firmId,

        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be greater than zero")
        Long userId
) {
}