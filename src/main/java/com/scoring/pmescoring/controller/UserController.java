package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.user.UpdateUserRequest;
import com.scoring.pmescoring.dto.request.user.UserLogin;
import com.scoring.pmescoring.dto.request.user.UserRequest;
import com.scoring.pmescoring.dto.response.user.DataTokenResponse;
import com.scoring.pmescoring.dto.response.user.UserResponse;
import com.scoring.pmescoring.service.UserService;
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
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRequest userRequest) {
        log.info("Received request to register new user");

        UserResponse userResponse = userService.create(userRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userResponse.id())
                .toUri();

        log.info("User registered successfully with ID: {}", userResponse.id());
        return ResponseEntity.created(location).body(userResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable(name = "id") Long id) {
        log.info("Fetching user with ID: {}", id);
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        log.info("Fetching paginated users. PageNumber: {}, PageSize: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable(name = "id") Long id,
                                               @RequestBody @Valid UpdateUserRequest userRequest) {
        log.info("Received request to update user with ID: {}", id);

        UserResponse response = userService.update(id, userRequest);

        log.info("User with ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        log.info("Received request to delete user with ID: {}", id);

        userService.delete(id);

        log.info("User with ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<DataTokenResponse> login(@RequestBody @Valid UserLogin login) {
        log.info("Received login authentication request");

        DataTokenResponse response = userService.login(login);

        log.info("User authenticated successfully. Token generated.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/valid/{token}")
    public ResponseEntity<Void> validation(@PathVariable String token) {
        log.info("Received request to validate authentication token");

        this.userService.validToken(token);

        log.info("Authentication token validated successfully");
        return ResponseEntity.ok().build();
    }
}