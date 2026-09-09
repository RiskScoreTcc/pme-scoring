package com.scoring.pmescoring.dto.response.defaultoccurrence;

import com.scoring.pmescoring.model.EntityStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DefaultOccurrenceResponse(
        Long id,
        Long firmId,
        LocalDate dateOccurrence,
        BigDecimal amountDue,
        Boolean statusResolved,
        String description,
        EntityStatus status,
        LocalDate creationDate
) {
}
