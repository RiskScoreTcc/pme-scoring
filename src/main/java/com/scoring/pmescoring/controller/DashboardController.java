package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.response.firm.FirmRiskResponse;
import com.scoring.pmescoring.model.RiskBand;
import com.scoring.pmescoring.service.DashboardService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/companies/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/firms")
    public ResponseEntity<Page<FirmRiskResponse>> getFirmsDashboard(
            @RequestParam(required = false) RiskBand risk,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(dashboardService.getFirms(risk, pageable));
    }

    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportLargeCsv(
            @RequestParam(required = false) RiskBand risk,
            @RequestParam(defaultValue = "5000") int pageSize
    ) {
        String fileName = "firms-report-" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) +
                ".csv";

        StreamingResponseBody stream = outputStream ->
                dashboardService.exportFirmsWithScoresInCsv(
                        risk,
                        pageSize,
                        outputStream
                );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(fileName)
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(stream);
    }
}