package com.scoring.pmescoring.dto.request;

import com.scoring.pmescoring.model.FormulaType;
import java.math.BigDecimal;

public record UpdateWeightConfigurationRequest(
        FormulaType formulaType,
        BigDecimal revenueWeight,
        BigDecimal timeWeight,
        BigDecimal defaultWeight
) {
}
