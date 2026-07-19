package com.rapauth.rapauth.controller;

import com.rapauth.rapauth.dto.auth.LoginRequest;
import com.rapauth.rapauth.dto.auth.LoginResponse;
import com.rapauth.rapauth.dto.auth.RegenerateRequest;
import com.rapauth.rapauth.dto.auth.RegenerateResponse;
import com.rapauth.rapauth.dto.auth.TimeoutRequest;
import com.rapauth.rapauth.dto.auth.TimeoutResponse;
import com.rapauth.rapauth.dto.auth.VerifyLoginRequest;
import com.rapauth.rapauth.dto.auth.VerifyLoginResponse;
import com.rapauth.rapauth.dto.user.UserRequest;
import com.rapauth.rapauth.dto.user.UserResponse;
import com.rapauth.rapauth.service.AuthService;
import com.rapauth.rapauth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Authentication operations")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Initiate RAP login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.initiateLogin(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyLoginResponse> verifyLogin(@RequestBody VerifyLoginRequest request) {
        return ResponseEntity.ok(authService.verifyLogin(request));
    }

    @PostMapping("/regenerate")
    public ResponseEntity<RegenerateResponse> regenerateAuthentication(
            @RequestBody RegenerateRequest request) {

        return ResponseEntity.ok(
                authService.regenerateAuthentication(request)
        );
    }

    @PostMapping("/timeout")
    public ResponseEntity<TimeoutResponse> handleTimeout(
            @RequestBody TimeoutRequest request) {
        return ResponseEntity.ok(authService.handleTimeout(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user with credentials for enrolled methods")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }
}