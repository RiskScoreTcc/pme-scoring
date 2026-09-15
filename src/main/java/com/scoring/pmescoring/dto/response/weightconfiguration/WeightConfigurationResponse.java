package com.scoring.pmescoring.dto.response.weightconfiguration;

import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.FormulaType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Representation of a scoring weight configuration version")
public record WeightConfigurationResponse(

        @Schema(description = "Unique internal identifier of the configuration record", example = "1")
        Long id,

        @Schema(description = "Mathematical formula type applied in the scoring engine", example = "WEIGHTED_AVERAGE")
        FormulaType formulaType,

        @Schema(description = "Weight assigned to the revenue factor in the scoring calculation", example = "0.40")
        BigDecimal revenueWeight,

        @Schema(description = "Weight assigned to the time/age factor in the scoring calculation", example = "0.30")
        BigDecimal timeWeight,

        @Schema(description = "Weight assigned to the default occurrences factor in the scoring calculation", example = "0.30")
        BigDecimal defaultWeight,

        @Schema(description = "Maximum revenue reference value used for normalization purposes", example = "500000.00")
        BigDecimal maxRevenueReference,

        @Schema(description = "Maximum time reference in months used for normalization purposes", example = "60")
        Integer maxTimeReferenceMonths,

        @Schema(description = "Threshold boundary separating medium risk from low risk", example = "75")
        Integer lowRiskThreshold,

        @Schema(description = "Threshold boundary separating high risk from medium risk", example = "50")
        Integer mediumRiskThreshold,

        @Schema(description = "Lifecycle status indicating if this configuration version is active or inactive", example = "ACTIVE")
        EntityStatus status,

        @Schema(description = "Date when this configuration version was created and registered", example = "2026-01-10")
        LocalDate creationDate,

        @Schema(description = "Identifier of the admin user who created or updated this configuration", example = "1")
        Long updatedByUserId
) {
}