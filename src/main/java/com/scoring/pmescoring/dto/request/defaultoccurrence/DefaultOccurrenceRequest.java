package com.scoring.pmescoring.dto.request.defaultoccurrence;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Payload for registering a new financial default occurrence or debt record for a firm")
public record DefaultOccurrenceRequest(

        @Schema(description = "Unique internal identifier of the firm associated with the default occurrence", example = "1")
        @NotNull(message = "Firm ID is required")
        @Positive(message = "Firm ID must be greater than zero")
        Long firmId,

        @Schema(description = "Date when the financial default occurrence took place (cannot be in the future)", example = "2026-08-20")
        @NotNull(message = "Occurrence date is required")
        @PastOrPresent(message = "Occurrence date cannot be in the future")
        LocalDate dateOccurrence,

        @Schema(description = "Financial amount due or debt value", example = "4500.00")
        @NotNull(message = "Amount due is required")
        @Positive(message = "Amount due must be greater than zero")
        @Digits(integer = 12, fraction = 2, message = "Amount due exceeds allowed limits or has invalid format")
        BigDecimal amountDue,

        @Schema(description = "Explanatory description or notes regarding the financial default", example = "Suppliers overdue invoice payment.")
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description
) {
}