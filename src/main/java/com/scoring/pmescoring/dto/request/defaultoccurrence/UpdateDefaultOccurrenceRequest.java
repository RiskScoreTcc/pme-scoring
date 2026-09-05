package com.scoring.pmescoring.dto.request.defaultoccurrence;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateDefaultOccurrenceRequest(
        Long firmId,
        LocalDate dateOccurrence,
        BigDecimal amountDue,
        String description
) {
}
