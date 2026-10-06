package com.scoring.pmescoring.dto.request.defaultoccurrence;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Filter criteria for querying default occurrences associated with active firms")
public record DefaultOccurrenceFilter(

        @Schema(
                description = "Filter by resolution status of the occurrence. Set 'true' for resolved/paid occurrences, 'false' for pending/unsettled occurrences, or omit/null to retrieve both",
                example = "false"
        )
        Boolean statusResolved,

        @Schema(
                description = "Search term to match against the active firm's registered company name or CNPJ. Supports case-insensitive partial text matching",
                example = "12345678000195"
        )
        String query

) {
}