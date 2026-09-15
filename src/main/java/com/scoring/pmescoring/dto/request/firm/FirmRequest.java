package com.scoring.pmescoring.dto.request.firm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

import java.math.BigDecimal;

@Schema(description = "Payload for registering a new SME firm profile in the system")
public record FirmRequest(

        @Schema(description = "Unique internal identifier of the user registering the firm", example = "1")
        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be greater than zero")
        Long userId,

        @Schema(description = "Official tax registration number of the firm (CNPJ) validated via checksum", example = "12.345.678/0001-99")
        @NotBlank(message = "CNPJ is required")
        @CNPJ(message = "Invalid CNPJ format or checksum")
        String cnpj,

        @Schema(description = "Official registered legal name of the company", example = "Tech Solutions Inovação Ltda")
        @NotBlank(message = "Registered company name is required")
        @Size(max = 255, message = "Registered company name must not exceed 255 characters")
        String registeredCompanyName,

        @Schema(description = "Average revenue reported by the firm (supports up to 12 integer digits and 2 decimals)", example = "150000.00")
        @NotNull(message = "Average revenue is required")
        @PositiveOrZero(message = "Average revenue cannot be negative")
        @Digits(integer = 12, fraction = 2, message = "Average revenue exceeds allowed limits or has invalid format")
        BigDecimal averageRevenue,

        @Schema(description = "Age of the company expressed in total months", example = "48")
        @NotNull(message = "Age in months is required")
        @PositiveOrZero(message = "Age in months cannot be negative")
        Integer ageInMonths,

        @Schema(description = "Total number of active employees working at the company", example = "25")
        @NotNull(message = "Number of employees is required")
        @PositiveOrZero(message = "Number of employees cannot be negative")
        Integer numberOfEmployees
) {
}