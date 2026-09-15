package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.defaultoccurrence.DefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.request.defaultoccurrence.UpdateDefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.response.defaultoccurrence.DefaultOccurrenceResponse;
import com.scoring.pmescoring.service.DefaultOccurrenceService;
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
@RequestMapping("/api/v1/default-occurrences")
@RequiredArgsConstructor
@Tag(name = "Default Occurrences", description = "Endpoints for managing financial defaults, debts, and payment issues of SMEs")
public class DefaultOccurrenceController {

    private final DefaultOccurrenceService defaultOccurrenceService;

    @PostMapping
    @Operation(summary = "Register a default occurrence", description = "Records a new financial default or payment issue for a specific firm. Impacts the firm's risk score.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Default occurrence registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)"),
            @ApiResponse(responseCode = "404", description = "Firm not found")
    })
    public ResponseEntity<DefaultOccurrenceResponse> register(@RequestBody @Valid DefaultOccurrenceRequest defaultOccurrenceRequest) {
        log.info("Received request to register default occurrence");

        DefaultOccurrenceResponse defaultOccurrenceResponse = defaultOccurrenceService.create(defaultOccurrenceRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(defaultOccurrenceResponse.id())
                .toUri();

        log.info("Default occurrence registered successfully with ID: {}", defaultOccurrenceResponse.id());
        return ResponseEntity.created(location).body(defaultOccurrenceResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve occurrence by ID", description = "Fetches the details of a specific active default occurrence using its internal ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Occurrence retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)"),
            @ApiResponse(responseCode = "404", description = "Default occurrence not found or inactive")
    })
    public ResponseEntity<DefaultOccurrenceResponse> getDefaultOccurrence(@PathVariable(name = "id") Long id) {
        log.info("Fetching default occurrence with ID: {}", id);
        return ResponseEntity.ok(defaultOccurrenceService.findById(id));
    }

    @GetMapping
    @Operation(summary = "List all active occurrences", description = "Retrieves a paginated list of all active default occurrences recorded in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)")
    })
    public ResponseEntity<Page<DefaultOccurrenceResponse>> getAllDefaultOccurrences(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        log.info("Fetching paginated default occurrences. PageNumber: {}, PageSize: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(defaultOccurrenceService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update occurrence details", description = "Updates specific details (such as amount due, date, or description) of an active default occurrence.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Occurrence updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)"),
            @ApiResponse(responseCode = "404", description = "Default occurrence not found")
    })
    public ResponseEntity<DefaultOccurrenceResponse> update(@PathVariable(name = "id") Long id,
                                                            @RequestBody @Valid UpdateDefaultOccurrenceRequest updateDefaultOccurrenceRequest) {
        log.info("Received request to update default occurrence with ID: {}", id);

        DefaultOccurrenceResponse response = defaultOccurrenceService.update(id, updateDefaultOccurrenceRequest);

        log.info("Default occurrence with ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Resolve a default occurrence", description = "Marks an active default occurrence as resolved, indicating that the debt has been paid or settled. This positively impacts future risk score calculations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Occurrence marked as resolved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)"),
            @ApiResponse(responseCode = "404", description = "Default occurrence not found")
    })
    public ResponseEntity<DefaultOccurrenceResponse> updateStatus(@PathVariable(name = "id") Long id) {
        log.info("Received request to update status for default occurrence with ID: {}", id);

        DefaultOccurrenceResponse response = defaultOccurrenceService.updateStatus(id);

        log.info("Status for default occurrence with ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Logically delete an occurrence", description = "Marks a default occurrence as deleted. This is used for erroneous entries, not for settled debts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Occurrence deleted successfully (No Content)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)"),
            @ApiResponse(responseCode = "404", description = "Default occurrence not found")
    })
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        log.info("Received request to delete default occurrence with ID: {}", id);

        defaultOccurrenceService.delete(id);

        log.info("Default occurrence with ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}