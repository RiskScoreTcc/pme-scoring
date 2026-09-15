package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.dto.request.user.UpdateUserRequest;
import com.scoring.pmescoring.dto.request.user.UserRequest;
import com.scoring.pmescoring.dto.response.user.UserResponse;
import com.scoring.pmescoring.service.UserService;
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

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRequest userRequest) {

        UserResponse userResponse = userService.create(userRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userResponse.id())
                .toUri();

        return ResponseEntity.created(location).body(userResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(@PageableDefault(size = 10, sort = {"id"}) Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable(name = "id") Long id,
                                               @RequestBody @Valid UpdateUserRequest userRequest) {
        return ResponseEntity.ok(userService.update(id, userRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}