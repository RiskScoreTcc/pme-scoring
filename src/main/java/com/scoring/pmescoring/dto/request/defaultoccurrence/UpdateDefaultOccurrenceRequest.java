package com.scoring.pmescoring.dto.request.defaultoccurrence;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Payload for updating an existing financial default occurrence or debt record")
public record UpdateDefaultOccurrenceRequest(

        @Schema(description = "Updated date when the financial default occurrence took place (cannot be in the future)", example = "2026-06-15")
        @PastOrPresent(message = "Occurrence date cannot be in the future")
        LocalDate dateOccurrence,

        @Schema(description = "Updated financial amount due or debt value", example = "12500.50")
        @Positive(message = "Amount due must be greater than zero")
        @Digits(integer = 12, fraction = 2, message = "Amount due exceeds allowed limits or has invalid format")
        BigDecimal amountDue,

        @Schema(description = "Updated explanatory description or notes regarding the financial default", example = "Commercial bank loan installment overdue by 45 days.")
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description
) {
}