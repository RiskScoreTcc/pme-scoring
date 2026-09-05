package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.calculatedscore.CalculatedScoreRequest;
import com.scoring.pmescoring.dto.request.calculatedscore.UpdateCalculatedScoreRequest;
import com.scoring.pmescoring.dto.response.calculatedscore.CalculatedScoreResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/calculated-scores")
public class CalculatedScoreController {

    @PostMapping
    public ResponseEntity<CalculatedScoreResponse> register(@RequestBody @Valid CalculatedScoreRequest calculatedScoreRequest) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(1)
                .toUri();

        return ResponseEntity.created(location).body(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalculatedScoreResponse> getCalculatedScore(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(null);
    }

    @GetMapping
    public ResponseEntity<Page<CalculatedScoreResponse>> getAllCalculatedScores(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CalculatedScoreResponse> update(@PathVariable(name = "id") Long id,
                                                          @RequestBody @Valid UpdateCalculatedScoreRequest updateCalculatedScoreRequest) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        return ResponseEntity.noContent().build();
    }
}