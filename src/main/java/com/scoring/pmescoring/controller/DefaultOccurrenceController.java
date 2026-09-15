package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.defaultoccurrence.DefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.request.defaultoccurrence.UpdateDefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.response.defaultoccurrence.DefaultOccurrenceResponse;
import com.scoring.pmescoring.service.DefaultOccurrenceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/default-occurrences")
public class DefaultOccurrenceController {

    private final DefaultOccurrenceService defaultOccurrenceService;

    public DefaultOccurrenceController(DefaultOccurrenceService defaultOccurrenceService) {
        this.defaultOccurrenceService = defaultOccurrenceService;
    }

    @PostMapping
    public ResponseEntity<DefaultOccurrenceResponse> register(@RequestBody @Valid DefaultOccurrenceRequest defaultOccurrenceRequest) {

        DefaultOccurrenceResponse defaultOccurrenceResponse =  defaultOccurrenceService.create(defaultOccurrenceRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(defaultOccurrenceResponse.id())
                .toUri();

        return ResponseEntity.created(location).body(defaultOccurrenceResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultOccurrenceResponse> getDefaultOccurrence(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(defaultOccurrenceService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<DefaultOccurrenceResponse>> getAllDefaultOccurrences(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        return ResponseEntity.ok(defaultOccurrenceService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DefaultOccurrenceResponse> update(@PathVariable(name = "id") Long id,
                                                            @RequestBody @Valid UpdateDefaultOccurrenceRequest updateDefaultOccurrenceRequest) {
        return ResponseEntity.ok(defaultOccurrenceService.update(id, updateDefaultOccurrenceRequest));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DefaultOccurrenceResponse> updateStatus(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(defaultOccurrenceService.updateStatus(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        defaultOccurrenceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}