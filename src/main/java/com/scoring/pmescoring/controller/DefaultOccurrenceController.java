package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.defaultoccurrence.DefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.request.defaultoccurrence.UpdateDefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.response.defaultoccurrence.DefaultOccurrenceResponse;
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

    @PostMapping
    public ResponseEntity<DefaultOccurrenceResponse> register(@RequestBody @Valid DefaultOccurrenceRequest defaultOccurrenceRequest) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(1)
                .toUri();

        return ResponseEntity.created(location).body(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DefaultOccurrenceResponse> getDefaultOccurrence(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(null);
    }

    @GetMapping
    public ResponseEntity<Page<DefaultOccurrenceResponse>> getAllDefaultOccurrences(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DefaultOccurrenceResponse> update(@PathVariable(name = "id") Long id,
                                                            @RequestBody @Valid UpdateDefaultOccurrenceRequest updateDefaultOccurrenceRequest) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        return ResponseEntity.noContent().build();
    }
}