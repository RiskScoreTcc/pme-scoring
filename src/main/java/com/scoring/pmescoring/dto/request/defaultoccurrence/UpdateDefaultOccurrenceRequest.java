package com.scoring.pmescoring.dto.request.defaultoccurrence;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateDefaultOccurrenceRequest(
        @PastOrPresent(message = "Occurrence date cannot be in the future")
        LocalDate dateOccurrence,

        @Positive(message = "Amount due must be greater than zero")
        @Digits(integer = 12, fraction = 2, message = "Amount due exceeds allowed limits or has invalid format")
        BigDecimal amountDue,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description
) {
}