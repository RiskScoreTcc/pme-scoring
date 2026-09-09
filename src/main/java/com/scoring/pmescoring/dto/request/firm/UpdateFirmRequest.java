package com.scoring.pmescoring.dto.request.firm;

import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.br.CNPJ;

import java.math.BigDecimal;

public record UpdateFirmRequest(
        @CNPJ(message = "Invalid CNPJ format")
        String cnpj,
        String registeredCompanyName,
        @PositiveOrZero(message = "Age in months cannot be negative")
        BigDecimal averageRevenue,
        @PositiveOrZero(message = "Age in months cannot be negative")
        Integer ageInMonths,
        @PositiveOrZero(message = "Age in months cannot be negative")
        Integer numberOfEmployees
) {
}
