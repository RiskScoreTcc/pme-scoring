package com.scoring.pmescoring.dto.response.calculatedscore;

import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.RiskBand;
import com.scoring.pmescoring.model.ScoreFactorsDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Detailed representation of a calculated risk score")
public record CalculatedScoreResponse(

        @Schema(description = "Unique internal identifier of the score record", example = "1054")
        Long id,

        @Schema(description = "Identifier of the evaluated firm", example = "1")
        Long firmId,

        @Schema(description = "Identifier of the user who requested the evaluation", example = "42")
        Long userId,

        @Schema(description = "Final calculated score value (typically from 0 to 100)", example = "85")
        Integer scoreValue,

        @Schema(description = "Risk categorization based on the final score value", example = "LOW_RISK")
        RiskBand riskBand,

        @Schema(description = "Current lifecycle status of this score (e.g., ACTIVE or INACTIVE)", example = "ACTIVE")
        EntityStatus status,

        @Schema(description = "Detailed breakdown of the mathematical factors and weights applied during calculation")
        ScoreFactorsDTO factorsJson,

        @Schema(description = "System-generated narrative justifying the final score", example = "Firm presents strong revenue history and no recent defaults.")
        String justification,

        @Schema(description = "Date when the calculation was executed", example = "2026-09-13")
        LocalDate calculationDate,

        @Schema(description = "Business or legal disclaimer regarding the automated calculation", example = "This score is indicative. Manual review is required for credit lines exceeding $50k.")
        String disclaimer
) {
}