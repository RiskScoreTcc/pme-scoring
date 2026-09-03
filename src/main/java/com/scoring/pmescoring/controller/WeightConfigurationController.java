package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.UpdateWeightConfigurationRequest;
import com.scoring.pmescoring.dto.request.WeightConfigurationRequest;
import com.scoring.pmescoring.dto.response.WeightConfigurationResponse;
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

    @PostMapping
    public ResponseEntity<WeightConfigurationResponse> register(@RequestBody @Valid WeightConfigurationRequest weightConfigurationRequest) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(1)
                .toUri();

        return ResponseEntity.created(location).body(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WeightConfigurationResponse> getWeightConfiguration(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(null);
    }

    @GetMapping
    public ResponseEntity<Page<WeightConfigurationResponse>> getAllWeightConfigurations(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WeightConfigurationResponse> update(@PathVariable(name = "id") Long id,
                                                              @RequestBody @Valid UpdateWeightConfigurationRequest updateWeightConfigurationRequest) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        return ResponseEntity.noContent().build();
    }
}