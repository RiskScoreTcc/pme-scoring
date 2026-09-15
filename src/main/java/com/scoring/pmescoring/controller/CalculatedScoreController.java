package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.calculatedscore.CalculatedScoreRequest;
import com.scoring.pmescoring.dto.request.calculatedscore.UpdateCalculatedScoreRequest;
import com.scoring.pmescoring.dto.response.calculatedscore.CalculatedScoreResponse;
import com.scoring.pmescoring.service.CalculatedScoreService;
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
@RequestMapping("/api/v1/calculated-scores")
@RequiredArgsConstructor
@Tag(name = "Calculated Scores", description = "Endpoints for managing SME risk scores and evaluations")
public class CalculatedScoreController {

    private final CalculatedScoreService calculatedScoreService;

    @PostMapping
    @Operation(summary = "Calculate a new risk score", description = "Creates a new calculated score for a specific firm and deactivates any previous active score.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Score calculated and registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)"),
            @ApiResponse(responseCode = "404", description = "Firm or User not found")
    })
    public ResponseEntity<CalculatedScoreResponse> register(@RequestBody @Valid CalculatedScoreRequest calculatedScoreRequest) {
        log.info("Received request to calculate new score");

        CalculatedScoreResponse scoreResponse = calculatedScoreService.create(calculatedScoreRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(scoreResponse.id())
                .toUri();

        log.info("Calculated score created successfully with ID: {}", scoreResponse.id());
        return ResponseEntity.created(location).body(scoreResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve score by ID", description = "Fetches the details of an active calculated score using its internal identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Calculated score retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)"),
            @ApiResponse(responseCode = "404", description = "Calculated score not found or inactive")
    })
    public ResponseEntity<CalculatedScoreResponse> getCalculatedScore(@PathVariable(name = "id") Long id) {
        log.info("Fetching calculated score with ID: {}", id);
        return ResponseEntity.ok(calculatedScoreService.findById(id));
    }

    @GetMapping
    @Operation(summary = "List all active scores", description = "Retrieves a paginated list of all active calculated scores in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)")
    })
    public ResponseEntity<Page<CalculatedScoreResponse>> getAllCalculatedScores(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        log.info("Fetching paginated calculated scores. PageNumber: {}, PageSize: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(calculatedScoreService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a calculated score", description = "Recalculates or updates the specified score based on new input parameters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Score updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)"),
            @ApiResponse(responseCode = "404", description = "Calculated score or Firm not found")
    })
    public ResponseEntity<CalculatedScoreResponse> update(@PathVariable(name = "id") Long id,
                                                          @RequestBody @Valid UpdateCalculatedScoreRequest updateCalculatedScoreRequest) {
        log.info("Received request to update calculated score with ID: {}", id);

        CalculatedScoreResponse response = calculatedScoreService.update(id, updateCalculatedScoreRequest);

        log.info("Calculated score with ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Logically delete a score", description = "Marks a calculated score as inactive. Business rules prevent deletion if it is the only active score.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Score deleted successfully (No Content)"),
            @ApiResponse(responseCode = "400", description = "Business rule violation (e.g., deleting the last active score)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires CREDIT_ANALYST role)"),
            @ApiResponse(responseCode = "404", description = "Calculated score not found")
    })
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        log.info("Received request to delete calculated score with ID: {}", id);

        calculatedScoreService.delete(id);

        log.info("Calculated score with ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}