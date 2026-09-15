package com.scoring.pmescoring.dto.request.firm;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

import java.math.BigDecimal;

public record UpdateFirmRequest(
        @CNPJ(message = "Invalid CNPJ format")
        String cnpj,

        @Size(max = 255, message = "Registered company name must not exceed 255 characters")
        String registeredCompanyName,

        @PositiveOrZero(message = "Average revenue cannot be negative")
        @Digits(integer = 12, fraction = 2, message = "Average revenue exceeds allowed limits or has invalid format")
        BigDecimal averageRevenue,

        @PositiveOrZero(message = "Age in months cannot be negative")
        Integer ageInMonths,

        @PositiveOrZero(message = "Number of employees cannot be negative")
        Integer numberOfEmployees
) {
}