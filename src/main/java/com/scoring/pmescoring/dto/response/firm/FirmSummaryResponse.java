package com.scoring.pmescoring.dto.response.firm;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary representation of a firm for listing and reference purposes")
public record FirmSummaryResponse(

        @Schema(description = "Unique internal identifier of the firm", example = "1")
        Long id,

        @Schema(description = "Official tax registration number of the firm (CNPJ)", example = "12.345.678/0001-99")
        String cnpj,

        @Schema(description = "Official registered legal name of the company", example = "Tech Solutions Inovação Ltda")
        String companyName
) {
}