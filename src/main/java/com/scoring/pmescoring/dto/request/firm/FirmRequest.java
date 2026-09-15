package com.scoring.pmescoring.dto.request.firm;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

import java.math.BigDecimal;

public record FirmRequest(

        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be greater than zero")
        Long userId,

        @NotBlank(message = "CNPJ is required")
        @CNPJ(message = "Invalid CNPJ format or checksum")
        String cnpj,

        @NotBlank(message = "Registered company name is required")
        @Size(max = 255, message = "Registered company name must not exceed 255 characters")
        String registeredCompanyName,

        @NotNull(message = "Average revenue is required")
        @PositiveOrZero(message = "Average revenue cannot be negative")
        @Digits(integer = 12, fraction = 2, message = "Average revenue exceeds allowed limits or has invalid format")
        BigDecimal averageRevenue,

        @NotNull(message = "Age in months is required")
        @PositiveOrZero(message = "Age in months cannot be negative")
        Integer ageInMonths,

        @NotNull(message = "Number of employees is required")
        @PositiveOrZero(message = "Number of employees cannot be negative")
        Integer numberOfEmployees
) {
}