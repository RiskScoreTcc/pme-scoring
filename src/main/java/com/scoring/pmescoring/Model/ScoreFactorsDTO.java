package com.scoring.pmescoring.Model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ScoreFactorsDTO(
        LocalDate calculationDate,
        BigDecimal averageRevenue,
        Integer timeMonths,
        Integer totalActiveDefaults,
        BigDecimal revenueWeight,
        BigDecimal timeWeight,
        BigDecimal defaultWeight,
        Boolean inDefault,
        BigDecimal amountDue
) {}