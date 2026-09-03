package com.scoring.pmescoring.dto.request;

import com.scoring.pmescoring.model.FormulaType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record WeightConfigurationRequest(
        @NotNull(message = "Formula type is required")
        FormulaType formulaType,

        @NotNull(message = "Revenue weight is required")
        @PositiveOrZero(message = "Revenue weight cannot be negative")
        @DecimalMax(value = "100.00", message = "Revenue weight cannot exceed 100.00")
        BigDecimal revenueWeight,

        @NotNull(message = "Time weight is required")
        @PositiveOrZero(message = "Time weight cannot be negative")
        @DecimalMax(value = "100.00", message = "Time weight cannot exceed 100.00")
        BigDecimal timeWeight,

        @NotNull(message = "Default weight is required")
        @PositiveOrZero(message = "Default weight cannot be negative")
        @DecimalMax(value = "100.00", message = "Default weight cannot exceed 100.00")
        BigDecimal defaultWeight
) {
}