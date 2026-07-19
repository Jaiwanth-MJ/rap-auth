package com.rapauth.rapauth.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimeoutResponse {
    private boolean locked;
    private String message;
    private String authenticationMethod;
    private Integer regenerationCount;
    private Integer expiresInSeconds;
}
