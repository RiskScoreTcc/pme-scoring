package com.scoring.pmescoring.service;

import com.scoring.pmescoring.dto.response.firm.FirmRiskResponse;
import com.scoring.pmescoring.model.RiskBand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.OutputStream;

public interface DashboardService {

    Page<FirmRiskResponse> getFirms(RiskBand riskBand, Pageable pageable);
    void exportFirmsWithScoresInCsv(RiskBand risk, int pageSize, OutputStream outputStream) throws IOException;
}
