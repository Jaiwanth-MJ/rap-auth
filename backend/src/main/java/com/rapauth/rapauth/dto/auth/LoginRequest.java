package com.rapauth.rapauth.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Register number is required")
    private String registerNumber;
}