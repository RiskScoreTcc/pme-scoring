package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.util.PageableSanitizer;
import com.scoring.pmescoring.dto.response.firm.FirmRiskResponse;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.RiskBand;
import com.scoring.pmescoring.repository.CalculatedScoreRepository;
import com.scoring.pmescoring.repository.DefaultOccurrenceRepository;
import com.scoring.pmescoring.repository.FirmRepository;
import com.scoring.pmescoring.service.DashboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final FirmRepository firmRepository;
    private final PageableSanitizer pageableSanitizer;

    public DashboardServiceImpl(FirmRepository firmRepository, PageableSanitizer pageableSanitizer) {
        this.firmRepository = firmRepository;
        this.pageableSanitizer = pageableSanitizer;
    }


    @Override
    @Transactional(readOnly = true)
    public Page<FirmRiskResponse> getFirms(RiskBand riskBand, Pageable pageable) {
        pageable = pageableSanitizer.sanitize(pageable);
        return firmRepository.findActiveFirmsByRiskBand(EntityStatus.ACTIVE, EntityStatus.ACTIVE, riskBand, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportFirmsWithScoresInCsv(RiskBand risk, int pageSize, OutputStream outputStream) throws IOException {
        int pageNumber = 0;
        boolean hasMorePages = true;

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

            writer.write("CNPJ,Razao Social,Score,Faixa de Risco,Justificativa\n");

            while (hasMorePages) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);

                Page<FirmRiskResponse> page = firmRepository.findActiveFirmsByRiskBand(
                        EntityStatus.ACTIVE,
                        EntityStatus.ACTIVE,
                        risk,
                        pageable
                );

                for (FirmRiskResponse firm : page.getContent()) {
                    writer.write(String.format(java.util.Locale.US, "\"%s\",\"%s\",%s,\"%s\",\"%s\"\n",
                            safeString(firm.cnpj()),
                            safeString(firm.registeredCompanyName()),
                            firm.scoreValue() != null ? firm.scoreValue() : "",
                            safeString(firm.riskBand()),
                            safeString(firm.justification())
                    ));
                }

                writer.flush();

                hasMorePages = page.hasNext();
                pageNumber++;
            }
        }
    }

    private String safeString(Object value) {
        return value != null ? value.toString().replace("\"", "\"\"") : "";
    }
}