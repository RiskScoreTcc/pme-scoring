package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.weightconfiguration.UpdateWeightConfigurationRequest;
import com.scoring.pmescoring.dto.request.weightconfiguration.WeightConfigurationRequest;
import com.scoring.pmescoring.dto.response.weightconfiguration.WeightConfigurationResponse;
import com.scoring.pmescoring.service.WeightConfigurationService;
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
@RequestMapping("/api/v1/weight-configurations")
@RequiredArgsConstructor
@Tag(name = "Weight Configurations", description = "Endpoints for managing the scoring engine parameters, risk thresholds, and formula weight configurations")
public class WeightConfigurationController {

    private final WeightConfigurationService weightConfigurationService;

    @PostMapping
    @Operation(summary = "Create a new weight configuration", description = "Registers a new parameter set for risk scoring and automatically deactivates the previous active configuration to ensure a single active state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Configuration created and activated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or risk thresholds validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ADMIN role)"),
            @ApiResponse(responseCode = "404", description = "Associated User not found")
    })
    public ResponseEntity<WeightConfigurationResponse> register(@RequestBody @Valid WeightConfigurationRequest weightConfigurationRequest) {
        log.info("Received request to register weight configuration");

        WeightConfigurationResponse weightConfigurationResponse = weightConfigurationService.create(weightConfigurationRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(weightConfigurationResponse.id())
                .toUri();

        log.info("Weight configuration registered successfully with ID: {}", weightConfigurationResponse.id());
        return ResponseEntity.created(location).body(weightConfigurationResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve configuration by ID", description = "Fetches a specific weight configuration using its internal identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ADMIN role)"),
            @ApiResponse(responseCode = "404", description = "Weight configuration not found")
    })
    public ResponseEntity<WeightConfigurationResponse> getWeightConfiguration(@PathVariable(name = "id") Long id) {
        log.info("Fetching weight configuration with ID: {}", id);
        return ResponseEntity.ok(weightConfigurationService.findById(id));
    }

    @GetMapping
    @Operation(summary = "List all weight configurations", description = "Retrieves a paginated history of all weight configurations (active and inactive).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ADMIN role)")
    })
    public ResponseEntity<Page<WeightConfigurationResponse>> getAllWeightConfigurations(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        log.info("Fetching paginated weight configurations. PageNumber: {}, PageSize: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(weightConfigurationService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update weight configuration", description = "Inactivates the current version and creates a new updated configuration version to preserve historical calculation accuracy.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or risk thresholds validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ADMIN role)"),
            @ApiResponse(responseCode = "404", description = "Weight configuration or User not found")
    })
    public ResponseEntity<WeightConfigurationResponse> update(@PathVariable(name = "id") Long id,
                                                              @RequestBody @Valid UpdateWeightConfigurationRequest updateWeightConfigurationRequest) {
        log.info("Received request to update weight configuration with ID: {}", id);

        WeightConfigurationResponse response = weightConfigurationService.update(id, updateWeightConfigurationRequest);

        log.info("Weight configuration with ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Logically delete a configuration", description = "Marks a weight configuration as deleted. The system enforces a business rule preventing the deletion of the last remaining active configuration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Configuration deleted successfully (No Content)"),
            @ApiResponse(responseCode = "400", description = "Business rule violation (e.g., trying to delete the only active configuration)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ADMIN role)"),
            @ApiResponse(responseCode = "404", description = "Weight configuration not found")
    })
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        log.info("Received request to delete weight configuration with ID: {}", id);

        weightConfigurationService.delete(id);

        log.info("Weight configuration with ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}