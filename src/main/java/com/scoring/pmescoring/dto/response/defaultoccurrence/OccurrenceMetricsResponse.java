package com.scoring.pmescoring.dto.response.defaultoccurrence;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Metrics summary of occurrences")
public record OccurrenceMetricsResponse(
        @Schema(description = "Total number of active occurrences", example = "10")
        long activeOccurrencesCount,

        @Schema(description = "Total number of open occurrences", example = "4")
        long openOccurrencesCount
) {
}
