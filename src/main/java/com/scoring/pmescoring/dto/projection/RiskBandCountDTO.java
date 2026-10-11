package com.scoring.pmescoring.dto.projection;

import io.swagger.v3.oas.annotations.media.Schema;

import com.scoring.pmescoring.model.RiskBand;

@Schema(description = "Projection DTO representing the total count of calculated scores categorized under a specific risk band level")
public record RiskBandCountDTO(
        @Schema(description = "Risk band classification level", example = "LOW")
        RiskBand riskBand,
        @Schema(description = "Total count of records classified under this risk band", example = "12")
        Long count
) {
}