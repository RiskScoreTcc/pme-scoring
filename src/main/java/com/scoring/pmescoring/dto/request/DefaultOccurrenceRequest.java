package com.scoring.pmescoring.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DefaultOccurrenceRequest(
        @NotNull(message = "Firm ID is required")
        Long firmId,

        @NotNull(message = "Occurrence date is required")
        @PastOrPresent(message = "Occurrence date cannot be in the future")
        LocalDate dateOccurrence,

        @NotNull(message = "Amount due is required")
        @Positive(message = "Amount due must be greater than zero")
        BigDecimal amountDue,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description
) {
}
