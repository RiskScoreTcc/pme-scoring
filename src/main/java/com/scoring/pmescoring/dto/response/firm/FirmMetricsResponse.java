package com.scoring.pmescoring.dto.response.firm;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Metrics summary of active firm counts grouped by risk levels")
public record FirmMetricsResponse(

        @Schema(description = "Count of active firms classified as Low Risk (e.g. BAND_A, BAND_B)", example = "45")
        long lowRiskCount,

        @Schema(description = "Count of active firms classified as Medium Risk (e.g. BAND_C)", example = "20")
        long mediumRiskCount,

        @Schema(description = "Count of active firms classified as High Risk (e.g. BAND_D, BAND_E)", example = "8")
        long highRiskCount,

        @Schema(description = "Total number of active firms evaluated with an active score", example = "73")
        long totalEvaluatedFirms
) {
}