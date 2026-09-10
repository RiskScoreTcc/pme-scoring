package com.scoring.pmescoring.dto.request.weightconfiguration;

import com.scoring.pmescoring.model.FormulaType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record WeightConfigurationRequest(
        @NotNull(message = "Formula type is required")
        FormulaType formulaType,

        @NotNull(message = "Revenue weight is required")
        @PositiveOrZero(message = "Revenue weight cannot be negative")
        @DecimalMax(value = "1.00", message = "Revenue weight cannot exceed 1.00")
        @Digits(integer = 1, fraction = 2, message = "Invalid format for revenue weight")
        BigDecimal revenueWeight,

        @NotNull(message = "Time weight is required")
        @PositiveOrZero(message = "Time weight cannot be negative")
        @DecimalMax(value = "1.00", message = "Time weight cannot exceed 1.00")
        @Digits(integer = 1, fraction = 2, message = "Invalid format for time weight")
        BigDecimal timeWeight,

        @NotNull(message = "Default weight is required")
        @PositiveOrZero(message = "Default weight cannot be negative")
        @DecimalMax(value = "1.00", message = "Default weight cannot exceed 1.00")
        @Digits(integer = 1, fraction = 2, message = "Invalid format for default weight")
        BigDecimal defaultWeight,

        @NotNull(message = "Max revenue reference is required")
        @Positive(message = "Max revenue reference must be greater than zero")
        @Digits(integer = 12, fraction = 2, message = "Max revenue reference exceeds allowed limits")
        BigDecimal maxRevenueReference,

        @NotNull(message = "Max time reference months is required")
        @Positive(message = "Max time reference months must be greater than zero")
        Integer maxTimeReferenceMonths,

        @NotNull(message = "Low risk threshold is required")
        @Positive(message = "Low risk threshold must be greater than zero")
        Integer lowRiskThreshold,

        @NotNull(message = "Medium risk threshold is required")
        @Positive(message = "Medium risk threshold must be greater than zero")
        Integer mediumRiskThreshold,

        @NotNull(message = "Updated by user ID is required")
        @Positive(message = "User ID must be greater than zero")
        Long updatedByUserId
) {
}