package com.rapauth.rapauth.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyLoginResponse {

    private boolean success;

    private String message;

    private String token;
}