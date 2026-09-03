package com.scoring.pmescoring.dto.request;

import com.scoring.pmescoring.model.RiskBand;

public record UpdateCalculatedScoreRequest(
        Long firmId,
        Integer scoreValue,
        RiskBand riskBand
) {
}
