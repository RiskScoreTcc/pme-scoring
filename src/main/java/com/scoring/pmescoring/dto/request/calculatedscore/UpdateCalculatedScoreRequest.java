package com.scoring.pmescoring.dto.request.calculatedscore;

import com.scoring.pmescoring.model.RiskBand;

public record UpdateCalculatedScoreRequest(
        Long firmId,
        Integer scoreValue,
        RiskBand riskBand
) {
}
