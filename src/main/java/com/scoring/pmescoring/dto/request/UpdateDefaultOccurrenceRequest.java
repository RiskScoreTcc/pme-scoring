package com.scoring.pmescoring.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateDefaultOccurrenceRequest(
        Long firmId,
        LocalDate dateOccurrence,
        BigDecimal amountDue,
        String description
) {
}
