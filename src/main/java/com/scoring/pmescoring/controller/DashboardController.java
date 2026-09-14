package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.response.firm.FirmRiskResponse;
import com.scoring.pmescoring.model.RiskBand;
import com.scoring.pmescoring.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api/v1/companies/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/firms")
    public ResponseEntity<Page<FirmRiskResponse>> getFirmsDashboard(
            @RequestParam(required = false) RiskBand risk,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        log.info("Fetching dashboard firms. RiskBand filter: {}, PageNumber: {}, PageSize: {}", risk, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(dashboardService.getFirms(risk, pageable));
    }

    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportLargeCsv(
            @RequestParam(required = false) RiskBand risk,
            @RequestParam(defaultValue = "5000") int pageSize
    ) {
        log.info("Received request to export CSV. RiskBand filter: {}, ChunkSize: {}", risk, pageSize);

        String fileName = "firms-report-" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) +
                ".csv";

        StreamingResponseBody stream = outputStream -> {
            log.info("Starting to write CSV data stream for file: {}", fileName);
            dashboardService.exportFirmsWithScoresInCsv(risk, pageSize, outputStream);
            log.info("Successfully finished writing CSV data stream for file: {}", fileName);
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(fileName)
                        .build()
        );

        log.info("Returning CSV stream response headers for file: {}", fileName);
        return ResponseEntity.ok()
                .headers(headers)
                .body(stream);
    }
}