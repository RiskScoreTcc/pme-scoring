package com.scoring.pmescoring.dto.response;

import com.scoring.pmescoring.model.FormulaType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WeightConfigurationResponse(
        Long id,
        FormulaType formulaType,
        BigDecimal revenueWeight,
        BigDecimal timeWeight,
        BigDecimal defaultWeight,
        Boolean active,
        LocalDate creationDate
) {
}
