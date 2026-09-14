package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.defaultoccurrence.DefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.request.defaultoccurrence.UpdateDefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.response.defaultoccurrence.DefaultOccurrenceResponse;
import com.scoring.pmescoring.service.DefaultOccurrenceService;
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
public class DefaultOccurrenceController {

    private final DefaultOccurrenceService defaultOccurrenceService;

    @PostMapping
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
    public ResponseEntity<DefaultOccurrenceResponse> getDefaultOccurrence(@PathVariable(name = "id") Long id) {
        log.info("Fetching default occurrence with ID: {}", id);
        return ResponseEntity.ok(defaultOccurrenceService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<DefaultOccurrenceResponse>> getAllDefaultOccurrences(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        log.info("Fetching paginated default occurrences. PageNumber: {}, PageSize: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(defaultOccurrenceService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DefaultOccurrenceResponse> update(@PathVariable(name = "id") Long id,
                                                            @RequestBody @Valid UpdateDefaultOccurrenceRequest updateDefaultOccurrenceRequest) {
        log.info("Received request to update default occurrence with ID: {}", id);

        DefaultOccurrenceResponse response = defaultOccurrenceService.update(id, updateDefaultOccurrenceRequest);

        log.info("Default occurrence with ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DefaultOccurrenceResponse> updateStatus(@PathVariable(name = "id") Long id) {
        log.info("Received request to update status for default occurrence with ID: {}", id);

        DefaultOccurrenceResponse response = defaultOccurrenceService.updateStatus(id);

        log.info("Status for default occurrence with ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        log.info("Received request to delete default occurrence with ID: {}", id);

        defaultOccurrenceService.delete(id);

        log.info("Default occurrence with ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}