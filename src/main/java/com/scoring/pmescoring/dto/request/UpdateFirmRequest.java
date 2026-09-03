package com.scoring.pmescoring.dto.request;

import java.math.BigDecimal;

public record UpdateFirmRequest(
        String cnpj,
        String registeredCompanyName,
        BigDecimal averageRevenue,
        Integer ageInMonths,
        Integer numberOfEmployees
) {
}
