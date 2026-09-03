package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.UpdateUserRequest;
import com.scoring.pmescoring.dto.request.UserRequest;
import com.scoring.pmescoring.dto.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    @PostMapping
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRequest userRequest) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(1)
                .toUri();

        return ResponseEntity.created(location).body(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(null);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(@PageableDefault(size = 10,sort = {"id"}) Pageable pageable) {
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable(name = "id") Long id,
                                       @RequestBody @Valid UpdateUserRequest userRequest) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        return ResponseEntity.noContent().build();
    }
}