package com.rapauth.rapauth.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private Long sessionId;

    private Long userId;

    private String fullName;

    private String registerNumber;

    private String authenticationMethod;

    private int expiresInSeconds;
}