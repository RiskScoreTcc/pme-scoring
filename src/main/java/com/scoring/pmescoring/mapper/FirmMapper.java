package com.scoring.pmescoring.mapper;

import com.scoring.pmescoring.common.mapper.GenericMapper;
import com.scoring.pmescoring.domain.Firm;
import com.scoring.pmescoring.dto.request.firm.FirmRequest;
import com.scoring.pmescoring.dto.response.firm.FirmResponse;
import org.springframework.stereotype.Component;

@Component
public class FirmMapper implements GenericMapper<FirmRequest, FirmResponse, Firm> {
    @Override
    public Firm toEntity(FirmRequest firmRequest) {
        return new Firm(firmRequest.cnpj(), firmRequest.registeredCompanyName(), firmRequest.averageRevenue(), firmRequest.ageInMonths(), firmRequest.numberOfEmployees());
    }

    @Override
    public FirmResponse toResponse(Firm firm) {
        return new FirmResponse(firm.getId(), firm.getCnpj(), firm.getRegisteredCompanyName(), firm.getAverageRevenue(), firm.getTimeMonths(), firm.getNumberOfEmployees(), firm.getActive());
    }
}
