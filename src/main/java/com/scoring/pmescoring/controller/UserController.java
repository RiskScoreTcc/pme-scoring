package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.user.UpdateUserRequest;
import com.scoring.pmescoring.dto.request.user.UserLogin;
import com.scoring.pmescoring.dto.request.user.UserRequest;
import com.scoring.pmescoring.dto.response.user.DataTokenResponse;
import com.scoring.pmescoring.dto.response.user.UserResponse;
import com.scoring.pmescoring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Users & Authentication", description = "Endpoints for Identity Management (IAM), user profiles, and JWT authentication")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Register a new user", description = "Creates a new user account (Admin or Credit Analyst). The system enforces email uniqueness.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or email already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ADMIN role)")
    })
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
    @Operation(summary = "Retrieve user by ID", description = "Fetches the details of an active user using its internal identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ADMIN role)"),
            @ApiResponse(responseCode = "404", description = "User not found or inactive")
    })
    public ResponseEntity<UserResponse> getUser(@PathVariable(name = "id") Long id) {
        log.info("Fetching user with ID: {}", id);
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping
    @Operation(summary = "List all active users", description = "Retrieves a paginated list of all active system users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ADMIN role)")
    })
    public ResponseEntity<Page<UserResponse>> getAllUsers(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        log.info("Fetching paginated users. PageNumber: {}, PageSize: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update user details", description = "Updates an active user's details, including credentials or role type. Validates email uniqueness.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or email already in use by another user"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ADMIN role)"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> update(@PathVariable(name = "id") Long id,
                                               @RequestBody @Valid UpdateUserRequest userRequest) {
        log.info("Received request to update user with ID: {}", id);

        UserResponse response = userService.update(id, userRequest);

        log.info("User with ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Logically delete a user", description = "Marks a user account as deleted, preventing future logins while preserving their historical footprint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully (No Content)"),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ADMIN role)"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        log.info("Received request to delete user with ID: {}", id);

        userService.delete(id);

        log.info("User with ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Authenticate user (Login)", description = "Public endpoint to authenticate via email and password. Returns a JWT Bearer token valid for standard requests.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful, JWT generated"),
            @ApiResponse(responseCode = "400", description = "Invalid payload format"),
            @ApiResponse(responseCode = "403", description = "Bad credentials (invalid email or password)")
    })
    public ResponseEntity<DataTokenResponse> login(@RequestBody @Valid UserLogin login) {
        log.info("Received login authentication request");

        DataTokenResponse response = userService.login(login);

        log.info("User authenticated successfully. Token generated.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/valid/{token}")
    @SecurityRequirements
    @Operation(summary = "Validate a JWT Token", description = "Public endpoint to check if a specific JWT token is structurally valid, unexpired, and correctly signed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "403", description = "Token is invalid, malformed, or expired")
    })
    public ResponseEntity<Void> validation(@PathVariable String token) {
        log.info("Received request to validate authentication token");

        this.userService.validToken(token);

        log.info("Authentication token validated successfully");
        return ResponseEntity.ok().build();
    }
}