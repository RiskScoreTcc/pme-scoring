package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.firm.FirmRequest;
import com.scoring.pmescoring.dto.request.firm.UpdateFirmRequest;
import com.scoring.pmescoring.dto.response.firm.FirmResponse;
import com.scoring.pmescoring.service.FirmService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/companies")
public class FirmController {

    private final FirmService firmService;

    public FirmController(FirmService firmService) {
        this.firmService = firmService;
    }

    @PostMapping
    public ResponseEntity<FirmResponse> register(@RequestBody @Valid FirmRequest firmRequest) {

        FirmResponse firmResponse = firmService.create(firmRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(firmResponse.id())
                .toUri();

        return ResponseEntity.created(location).body(firmResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FirmResponse> getFirm(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(firmService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<FirmResponse>> getAllCompanies(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        return ResponseEntity.ok(firmService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FirmResponse> update(@PathVariable(name = "id") Long id,
                                               @RequestBody @Valid UpdateFirmRequest updateFirmRequest) {
        return ResponseEntity.ok(firmService.update(id, updateFirmRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        firmService.delete(id);
        return ResponseEntity.noContent().build();
    }
}