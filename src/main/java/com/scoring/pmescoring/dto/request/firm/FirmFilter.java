package com.scoring.pmescoring.dto.request.firm;

import com.scoring.pmescoring.model.RiskBand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Filter criteria for querying active firms by name, CNPJ, or assigned risk band")
public record FirmFilter(

        @Schema(
                description = "Search term to match against the active firm's registered company name, trade name, or CNPJ. Supports case-insensitive partial text matching",
                example = "12345678000195"
        )
        String query,

        @Schema(
                description = "Filter firms by their evaluated credit risk band classification",
                example = "HIGH"
        )
        RiskBand riskBand

) {
}
