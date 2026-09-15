package com.scoring.pmescoring.dto.response.firm;

import com.scoring.pmescoring.model.RiskBand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representation of firm risk summary used in dashboard views and analytical reporting")
public record FirmRiskResponse(

        @Schema(description = "Official tax registration number of the firm (CNPJ)", example = "12.345.678/0001-99")
        String cnpj,

        @Schema(description = "Official registered legal name of the company", example = "Tech Solutions Inovação Ltda")
        String registeredCompanyName,

        @Schema(description = "Final calculated risk score assigned to the firm", example = "82")
        Integer scoreValue,

        @Schema(description = "Risk band classification based on the score value", example = "LOW_RISK")
        RiskBand riskBand,

        @Schema(description = "Analytical narrative or justification explaining the assigned risk band", example = "Firm presents robust average revenue, healthy age, and zero default occurrences.")
        String justification
) {
}