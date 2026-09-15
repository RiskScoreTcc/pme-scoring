package com.scoring.pmescoring.dto.response.firm;

import com.scoring.pmescoring.model.EntityStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Detailed representation of an SME firm profile")
public record FirmResponse(

        @Schema(description = "Unique internal identifier of the firm", example = "1")
        Long id,

        @Schema(description = "Official tax registration number of the firm (CNPJ)", example = "12.345.678/0001-99")
        String cnpj,

        @Schema(description = "Official registered legal name of the company", example = "Tech Solutions Inovação Ltda")
        String registeredCompanyName,

        @Schema(description = "Average monthly or annual revenue reported by the firm", example = "150000.00")
        BigDecimal averageRevenue,

        @Schema(description = "Age of the company expressed in total months", example = "48")
        Integer ageInMonths,

        @Schema(description = "Total number of active employees working at the company", example = "25")
        Integer numberOfEmployees,

        @Schema(description = "Current lifecycle status of the firm record (ACTIVE or INACTIVE)", example = "ACTIVE")
        EntityStatus status
) {
}