package com.scoring.pmescoring.dto.response.calculatedscore;

import com.scoring.pmescoring.model.RiskBand;
import com.scoring.pmescoring.model.ScoreFactorsDTO;

import java.time.LocalDate;

public record CalculatedScoreResponse(
        Long id,
        Long firmId,
        Integer scoreValue,
        RiskBand riskBand,
        Boolean active,
        ScoreFactorsDTO factorsJson,
        LocalDate calculationDate
) {
}