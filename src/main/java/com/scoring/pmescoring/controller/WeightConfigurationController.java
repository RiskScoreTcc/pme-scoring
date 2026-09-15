package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.weightconfiguration.UpdateWeightConfigurationRequest;
import com.scoring.pmescoring.dto.request.weightconfiguration.WeightConfigurationRequest;
import com.scoring.pmescoring.dto.response.weightconfiguration.WeightConfigurationResponse;
import com.scoring.pmescoring.service.WeightConfigurationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/weight-configurations")
public class WeightConfigurationController {

    private final WeightConfigurationService weightConfigurationService;

    public WeightConfigurationController(WeightConfigurationService weightConfigurationService) {
        this.weightConfigurationService = weightConfigurationService;
    }

    @PostMapping
    public ResponseEntity<WeightConfigurationResponse> register(@RequestBody @Valid WeightConfigurationRequest weightConfigurationRequest) {
        WeightConfigurationResponse weightConfigurationResponse = weightConfigurationService.create(weightConfigurationRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(weightConfigurationResponse.id())
                .toUri();

        return ResponseEntity.created(location).body(weightConfigurationResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WeightConfigurationResponse> getWeightConfiguration(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(weightConfigurationService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<WeightConfigurationResponse>> getAllWeightConfigurations(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        return ResponseEntity.ok(weightConfigurationService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WeightConfigurationResponse> update(@PathVariable(name = "id") Long id,
                                                              @RequestBody @Valid UpdateWeightConfigurationRequest updateWeightConfigurationRequest) {
        return ResponseEntity.ok(weightConfigurationService.update(id, updateWeightConfigurationRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        weightConfigurationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}