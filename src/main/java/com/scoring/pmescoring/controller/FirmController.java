package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.firm.FirmRequest;
import com.scoring.pmescoring.dto.request.firm.UpdateFirmRequest;
import com.scoring.pmescoring.dto.response.firm.FirmResponse;
import com.scoring.pmescoring.service.FirmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Companies (Firms)", description = "Endpoints for managing SME profiles, basic information, and core data")
public class FirmController {

    private final FirmService firmService;

    @PostMapping
    @Operation(summary = "Register a new company", description = "Creates a new SME profile in the system. The system validates if the provided CNPJ is unique among active companies.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Company registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or CNPJ already in use"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)"),
            @ApiResponse(responseCode = "404", description = "Requesting User not found")
    })
    public ResponseEntity<FirmResponse> register(@RequestBody @Valid FirmRequest firmRequest) {
        log.info("Received request to register firm");

        FirmResponse firmResponse = firmService.create(firmRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(firmResponse.id())
                .toUri();

        log.info("Firm registered successfully with ID: {}", firmResponse.id());
        return ResponseEntity.created(location).body(firmResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve company by ID", description = "Fetches the detailed profile of a specific active company using its internal ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company profile retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)"),
            @ApiResponse(responseCode = "404", description = "Company not found or inactive")
    })
    public ResponseEntity<FirmResponse> getFirm(@PathVariable(name = "id") Long id) {
        log.info("Fetching firm with ID: {}", id);
        return ResponseEntity.ok(firmService.findById(id));
    }

    @GetMapping
    @Operation(summary = "List all active companies", description = "Retrieves a paginated list of all active SME profiles registered in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)")
    })
    public ResponseEntity<Page<FirmResponse>> getAllCompanies(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        log.info("Fetching paginated firms. PageNumber: {}, PageSize: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(firmService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update company details", description = "Updates specific details of an active company, such as revenue, age, or CNPJ. Ensures CNPJ uniqueness upon update.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Company updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or CNPJ already in use by another company"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    public ResponseEntity<FirmResponse> update(@PathVariable(name = "id") Long id,
                                               @RequestBody @Valid UpdateFirmRequest updateFirmRequest) {
        log.info("Received request to update firm with ID: {}", id);

        FirmResponse response = firmService.update(id, updateFirmRequest);

        log.info("Firm with ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Logically delete a company (Cascade)", description = "Marks a company as deleted. This operation performs a cascading logical deletion on all calculated scores and default occurrences associated with this company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Company and its dependencies deleted successfully (No Content)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        log.info("Received request to delete firm with ID: {}", id);

        firmService.delete(id);

        log.info("Firm with ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}