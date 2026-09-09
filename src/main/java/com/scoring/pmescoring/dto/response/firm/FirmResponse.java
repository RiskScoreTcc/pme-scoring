package com.scoring.pmescoring.dto.response.firm;

import com.scoring.pmescoring.model.EntityStatus;

import java.math.BigDecimal;

public record FirmResponse(
        Long id,
        String cnpj,
        String registeredCompanyName,
        BigDecimal averageRevenue,
        Integer ageInMonths,
        Integer numberOfEmployees,
        EntityStatus status
) {
}