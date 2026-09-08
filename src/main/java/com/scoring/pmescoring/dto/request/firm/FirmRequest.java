package com.scoring.pmescoring.dto.request.firm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.br.CNPJ;

import java.math.BigDecimal;

public record FirmRequest(

        @NotNull(message = "User ID is required")
        Long userId,

        @NotBlank(message = "CNPJ is required")
        @CNPJ(message = "Invalid CNPJ format")
        String cnpj,

        @NotBlank(message = "Registered company name is required")
        String registeredCompanyName,

        @NotNull(message = "Average revenue is required")
        @PositiveOrZero(message = "Average revenue cannot be negative")
        BigDecimal averageRevenue,

        @NotNull(message = "Age in months is required")
        @PositiveOrZero(message = "Age in months cannot be negative")
        Integer ageInMonths,

        @NotNull(message = "Number of employees is required")
        @PositiveOrZero(message = "Number of employees cannot be negative")
        Integer numberOfEmployees
) {
}