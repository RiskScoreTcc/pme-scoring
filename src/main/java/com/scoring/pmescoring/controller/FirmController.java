package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.firm.FirmRequest;
import com.scoring.pmescoring.dto.request.firm.UpdateFirmRequest;
import com.scoring.pmescoring.dto.response.firm.FirmResponse;
import com.scoring.pmescoring.service.FirmService;
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
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class FirmController {

    private final FirmService firmService;

    @PostMapping
    public ResponseEntity<FirmResponse> register(@RequestBody @Valid FirmRequest firmRequest) {
        log.info("Received request to register firm");

        FirmResponse firmResponse = firmService.create(firmRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(firmResponse.id())
                .toUri();

        log.info("Firm registered successfully with ID: {}", firmResponse.id());
        return ResponseEntity.created(location).body(firmResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FirmResponse> getFirm(@PathVariable(name = "id") Long id) {
        log.info("Fetching firm with ID: {}", id);
        return ResponseEntity.ok(firmService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<FirmResponse>> getAllCompanies(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        log.info("Fetching paginated firms. PageNumber: {}, PageSize: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(firmService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FirmResponse> update(@PathVariable(name = "id") Long id,
                                               @RequestBody @Valid UpdateFirmRequest updateFirmRequest) {
        log.info("Received request to update firm with ID: {}", id);

        FirmResponse response = firmService.update(id, updateFirmRequest);

        log.info("Firm with ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        log.info("Received request to delete firm with ID: {}", id);

        firmService.delete(id);

        log.info("Firm with ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}