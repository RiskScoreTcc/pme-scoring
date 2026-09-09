package com.scoring.pmescoring.dto.response.calculatedscore;

import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.RiskBand;
import com.scoring.pmescoring.model.ScoreFactorsDTO;

import java.time.LocalDate;

public record CalculatedScoreResponse(
        Long id,
        Long firmId,
        Long userId,
        Integer scoreValue,
        RiskBand riskBand,
        EntityStatus status,
        ScoreFactorsDTO factorsJson,
        String justification,
        LocalDate calculationDate,
        String disclaimer
) {
}