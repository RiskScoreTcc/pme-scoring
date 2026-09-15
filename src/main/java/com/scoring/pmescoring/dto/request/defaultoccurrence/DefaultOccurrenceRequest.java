package com.scoring.pmescoring.dto.request.defaultoccurrence;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DefaultOccurrenceRequest(
        @NotNull(message = "Firm ID is required")
        @Positive(message = "Firm ID must be greater than zero")
        Long firmId,

        @NotNull(message = "Occurrence date is required")
        @PastOrPresent(message = "Occurrence date cannot be in the future")
        LocalDate dateOccurrence,

        @NotNull(message = "Amount due is required")
        @Positive(message = "Amount due must be greater than zero")
        @Digits(integer = 12, fraction = 2, message = "Amount due exceeds allowed limits or has invalid format")
        BigDecimal amountDue,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description
) {
}