package com.scoring.pmescoring.dto.response.weightconfiguration;

import com.scoring.pmescoring.model.FormulaType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WeightConfigurationResponse(
        Long id,
        FormulaType formulaType,
        BigDecimal revenueWeight,
        BigDecimal timeWeight,
        BigDecimal defaultWeight,
        BigDecimal maxRevenueReference,
        Integer maxTimeReferenceMonths,
        Boolean active,
        LocalDate creationDate
) {
}
