package com.scoring.pmescoring.dto.response.firm;

import com.scoring.pmescoring.model.RiskBand;

public record FirmRiskResponse(
        String cnpj,
        String registeredCompanyName,
        Integer scoreValue,
        RiskBand riskBand,
        String justification
) {
}
