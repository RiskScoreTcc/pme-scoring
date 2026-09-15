package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.util.PageableSanitizer;
import com.scoring.pmescoring.dto.response.firm.FirmRiskResponse;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.RiskBand;
import com.scoring.pmescoring.repository.FirmRepository;
import com.scoring.pmescoring.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final FirmRepository firmRepository;
    private final PageableSanitizer pageableSanitizer;

    @Override
    @Transactional(readOnly = true)
    public Page<FirmRiskResponse> getFirms(RiskBand riskBand, Pageable pageable) {
        log.info("Querying firms dashboard data. RiskBand filter: {}", riskBand);

        pageable = pageableSanitizer.sanitize(pageable);
        return firmRepository.findActiveFirmsByRiskBand(EntityStatus.ACTIVE, EntityStatus.ACTIVE, riskBand, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportFirmsWithScoresInCsv(RiskBand risk, int pageSize, OutputStream outputStream) throws IOException {
        log.info("Starting CSV export for active firms. RiskBand filter: {}, Batch size: {}", risk, pageSize);

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

                log.debug("Processed batch {} for CSV export", pageNumber);
            }

            log.info("CSV export completed successfully. Total batches processed: {}", pageNumber);
        } catch (IOException e) {
            log.error("Failed to write CSV export stream to output", e);
            throw e;
        }
    }

    private String safeString(Object value) {
        return value != null ? value.toString().replace("\"", "\"\"") : "";
    }
}