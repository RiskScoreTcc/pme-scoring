package com.scoring.pmescoring.dto.response.defaultoccurrence;

import com.scoring.pmescoring.model.EntityStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Representation of a financial default occurrence or debt record")
public record DefaultOccurrenceResponse(

        @Schema(description = "Unique internal identifier of the default occurrence record", example = "10")
        Long id,

        @Schema(description = "Identifier of the firm associated with this financial default", example = "1")
        Long firmId,

        @Schema(description = "Date when the financial default occurrence took place", example = "2026-08-20")
        LocalDate dateOccurrence,

        @Schema(description = "Financial amount due or debt value", example = "4500.00")
        BigDecimal amountDue,

        @Schema(description = "Flag indicating whether the default status has been resolved or settled", example = "false")
        Boolean statusResolved,

        @Schema(description = "Explanatory description or notes regarding the financial default", example = "Suppliers overdue invoice payment.")
        String description,

        @Schema(description = "Lifecycle status of the default record (ACTIVE or INACTIVE)", example = "ACTIVE")
        EntityStatus status,

        @Schema(description = "Date when this occurrence record was created in the system", example = "2026-08-21")
        LocalDate creationDate
) {
}