package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.calculatedscore.CalculatedScoreRequest;
import com.scoring.pmescoring.dto.request.calculatedscore.UpdateCalculatedScoreRequest;
import com.scoring.pmescoring.dto.response.calculatedscore.CalculatedScoreResponse;
import com.scoring.pmescoring.service.CalculatedScoreService;
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
public class CalculatedScoreController {

    private final CalculatedScoreService calculatedScoreService;

    @PostMapping
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
    public ResponseEntity<CalculatedScoreResponse> getCalculatedScore(@PathVariable(name = "id") Long id) {
        log.info("Fetching calculated score with ID: {}", id);
        return ResponseEntity.ok(calculatedScoreService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<CalculatedScoreResponse>> getAllCalculatedScores(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        log.info("Fetching paginated calculated scores. PageNumber: {}, PageSize: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(calculatedScoreService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CalculatedScoreResponse> update(@PathVariable(name = "id") Long id,
                                                          @RequestBody @Valid UpdateCalculatedScoreRequest updateCalculatedScoreRequest) {
        log.info("Received request to update calculated score with ID: {}", id);

        CalculatedScoreResponse response = calculatedScoreService.update(id, updateCalculatedScoreRequest);

        log.info("Calculated score with ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        log.info("Received request to delete calculated score with ID: {}", id);

        calculatedScoreService.delete(id);

        log.info("Calculated score with ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}