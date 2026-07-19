package com.rapauth.rapauth.controller;

import com.rapauth.rapauth.dto.user.UserResponse;
import com.rapauth.rapauth.entity.user.User;
import com.rapauth.rapauth.service.UserService;
import com.rapauth.rapauth.util.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "Operations related to users")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users")
    public List<UserResponse> getAllUsers() {
        return userService.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(UserMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    @Operation(summary = "Get current logged-in user")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(UserMapper.toResponse(user));
    }
}