package com.rapauth.rapauth.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegenerateResponse {

    private Long sessionId;
    private String authenticationMethod;
    private Integer regenerationCount;
    private Integer expiresInSeconds;

}