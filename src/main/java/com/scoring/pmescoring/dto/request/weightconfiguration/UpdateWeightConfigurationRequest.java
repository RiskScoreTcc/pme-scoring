package com.scoring.pmescoring.dto.request.weightconfiguration;

import com.scoring.pmescoring.model.FormulaType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Payload for updating or creating a new version of the scoring weight configuration parameters")
public record UpdateWeightConfigurationRequest(

        @Schema(description = "Mathematical formula type applied in the scoring engine", example = "WEIGHTED_AVERAGE")
        @NotNull(message = "Formula type is required")
        FormulaType formulaType,

        @Schema(description = "Weight assigned to the revenue factor (must be between 0.00 and 1.00)", example = "0.40")
        @NotNull(message = "Revenue weight is required")
        @PositiveOrZero(message = "Revenue weight cannot be negative")
        @DecimalMax(value = "1.00", message = "Revenue weight cannot exceed 1.00")
        @Digits(integer = 1, fraction = 2, message = "Invalid format for revenue weight")
        BigDecimal revenueWeight,

        @Schema(description = "Weight assigned to the time/age factor (must be between 0.00 and 1.00)", example = "0.30")
        @NotNull(message = "Time weight is required")
        @PositiveOrZero(message = "Time weight cannot be negative")
        @DecimalMax(value = "1.00", message = "Time weight cannot exceed 1.00")
        @Digits(integer = 1, fraction = 2, message = "Invalid format for time weight")
        BigDecimal timeWeight,

        @Schema(description = "Weight assigned to the default occurrences factor (must be between 0.00 and 1.00)", example = "0.30")
        @NotNull(message = "Default weight is required")
        @PositiveOrZero(message = "Default weight cannot be negative")
        @DecimalMax(value = "1.00", message = "Default weight cannot exceed 1.00")
        @Digits(integer = 1, fraction = 2, message = "Invalid format for default weight")
        BigDecimal defaultWeight,

        @Schema(description = "Maximum revenue reference value used for normalization boundaries", example = "500000.00")
        @NotNull(message = "Max revenue reference is required")
        @Positive(message = "Max revenue reference must be greater than zero")
        @Digits(integer = 12, fraction = 2, message = "Max revenue reference exceeds allowed limits")
        BigDecimal maxRevenueReference,

        @Schema(description = "Maximum time reference in months used for normalization boundaries", example = "60")
        @NotNull(message = "Max time reference months is required")
        @Positive(message = "Max time reference months must be greater than zero")
        Integer maxTimeReferenceMonths,

        @Schema(description = "Score threshold defining the boundary for low risk classification", example = "75")
        @NotNull(message = "Low risk threshold is required")
        @Positive(message = "Low risk threshold must be greater than zero")
        Integer lowRiskThreshold,

        @Schema(description = "Score threshold defining the boundary for medium risk classification", example = "50")
        @NotNull(message = "Medium risk threshold is required")
        @Positive(message = "Medium risk threshold must be greater than zero")
        Integer mediumRiskThreshold,

        @Schema(description = "Identifier of the administrator user performing this parameter update", example = "1")
        @NotNull(message = "Updated by user ID is required")
        @Positive(message = "User ID must be greater than zero")
        Long updatedByUserId
) {
}