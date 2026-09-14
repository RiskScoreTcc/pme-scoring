package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.weightconfiguration.UpdateWeightConfigurationRequest;
import com.scoring.pmescoring.dto.request.weightconfiguration.WeightConfigurationRequest;
import com.scoring.pmescoring.dto.response.weightconfiguration.WeightConfigurationResponse;
import com.scoring.pmescoring.service.WeightConfigurationService;
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
public class WeightConfigurationController {

    private final WeightConfigurationService weightConfigurationService;

    @PostMapping
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
    public ResponseEntity<WeightConfigurationResponse> getWeightConfiguration(@PathVariable(name = "id") Long id) {
        log.info("Fetching weight configuration with ID: {}", id);
        return ResponseEntity.ok(weightConfigurationService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<WeightConfigurationResponse>> getAllWeightConfigurations(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        log.info("Fetching paginated weight configurations. PageNumber: {}, PageSize: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(weightConfigurationService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WeightConfigurationResponse> update(@PathVariable(name = "id") Long id,
                                                              @RequestBody @Valid UpdateWeightConfigurationRequest updateWeightConfigurationRequest) {
        log.info("Received request to update weight configuration with ID: {}", id);

        WeightConfigurationResponse response = weightConfigurationService.update(id, updateWeightConfigurationRequest);

        log.info("Weight configuration with ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        log.info("Received request to delete weight configuration with ID: {}", id);

        weightConfigurationService.delete(id);

        log.info("Weight configuration with ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}