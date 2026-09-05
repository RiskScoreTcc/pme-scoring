package com.scoring.pmescoring.dto.request.calculatedscore;

import com.scoring.pmescoring.model.RiskBand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CalculatedScoreRequest(
        @NotNull(message = "Firm ID is required")
        Long firmId,

        @NotNull(message = "Score value is required")
        @PositiveOrZero(message = "Score value cannot be negative")
        Integer scoreValue,

        @NotNull(message = "Risk band is required")
        RiskBand riskBand
) {
}