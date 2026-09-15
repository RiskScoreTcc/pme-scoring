package com.scoring.pmescoring.dto.request.firm;

import java.math.BigDecimal;

public record UpdateFirmRequest(
        String cnpj,
        String registeredCompanyName,
        BigDecimal averageRevenue,
        Integer ageInMonths,
        Integer numberOfEmployees
) {
}
