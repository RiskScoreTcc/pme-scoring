package com.scoring.pmescoring.dto.response.firm;

import com.scoring.pmescoring.dto.response.calculatedscore.CalculatedScoreResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed response containing full firm profile and its calculated credit score metrics")
public record FirmDetailResponse(

        @Schema(description = "Core firm details, company profile, and registration status")
        FirmResponse firm,

        @Schema(description = "Latest calculated credit score metrics and risk analysis details")
        CalculatedScoreResponse calculatedScore

) {
}