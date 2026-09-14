package com.scoring.pmescoring.dto.request.firm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

import java.math.BigDecimal;

@Schema(description = "Payload for requesting an update to an existing SME firm profile")
public record UpdateFirmRequest(

        @Schema(description = "Updated official tax registration number of the firm (CNPJ)", example = "12.345.678/0001-99")
        @CNPJ(message = "Invalid CNPJ format")
        String cnpj,

        @Schema(description = "Updated official registered legal name of the company", example = "Tech Solutions Inovação Ltda")
        @Size(max = 255, message = "Registered company name must not exceed 255 characters")
        String registeredCompanyName,

        @Schema(description = "Updated average revenue reported by the firm", example = "180000.00")
        @PositiveOrZero(message = "Average revenue cannot be negative")
        @Digits(integer = 12, fraction = 2, message = "Average revenue exceeds allowed limits or has invalid format")
        BigDecimal averageRevenue,

        @Schema(description = "Updated age of the company expressed in total months", example = "60")
        @PositiveOrZero(message = "Age in months cannot be negative")
        Integer ageInMonths,

        @Schema(description = "Updated total number of active employees working at the company", example = "30")
        @PositiveOrZero(message = "Number of employees cannot be negative")
        Integer numberOfEmployees
) {
}