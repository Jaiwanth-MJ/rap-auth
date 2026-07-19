package com.rapauth.rapauth.dto.auth;

import lombok.Data;

@Data
public class VerifyLoginRequest {

    private Long sessionId;

    private String credential;
}