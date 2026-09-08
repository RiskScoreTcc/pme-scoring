package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.calculatedscore.CalculatedScoreRequest;
import com.scoring.pmescoring.dto.request.calculatedscore.UpdateCalculatedScoreRequest;
import com.scoring.pmescoring.dto.response.calculatedscore.CalculatedScoreResponse;
import com.scoring.pmescoring.service.CalculatedScoreService;
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

    private final CalculatedScoreService calculatedScoreService;

    public CalculatedScoreController(CalculatedScoreService calculatedScoreService) {
        this.calculatedScoreService = calculatedScoreService;
    }

    @PostMapping
    public ResponseEntity<CalculatedScoreResponse> register(@RequestBody @Valid CalculatedScoreRequest calculatedScoreRequest) {
        CalculatedScoreResponse scoreResponse = calculatedScoreService.create(calculatedScoreRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(scoreResponse.id())
                .toUri();

        return ResponseEntity.created(location).body(scoreResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalculatedScoreResponse> getCalculatedScore(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(calculatedScoreService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<CalculatedScoreResponse>> getAllCalculatedScores(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        return ResponseEntity.ok(calculatedScoreService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CalculatedScoreResponse> update(@PathVariable(name = "id") Long id,
                                                          @RequestBody @Valid UpdateCalculatedScoreRequest updateCalculatedScoreRequest) {
        return ResponseEntity.ok(calculatedScoreService.update(id, updateCalculatedScoreRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        calculatedScoreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}