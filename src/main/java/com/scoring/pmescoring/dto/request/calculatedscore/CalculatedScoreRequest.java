package com.scoring.pmescoring.dto.request.calculatedscore;

import jakarta.validation.constraints.NotNull;

public record CalculatedScoreRequest(
        @NotNull(message = "Firm ID is required")
        Long firmId,

        @NotNull(message = "User ID is required")
        Long userId
) {
}