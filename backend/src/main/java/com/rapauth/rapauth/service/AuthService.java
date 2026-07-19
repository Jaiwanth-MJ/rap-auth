package com.rapauth.rapauth.service;

import com.rapauth.rapauth.dto.auth.LoginRequest;
import com.rapauth.rapauth.dto.auth.LoginResponse;
import com.rapauth.rapauth.dto.auth.RegenerateRequest;
import com.rapauth.rapauth.dto.auth.RegenerateResponse;
import com.rapauth.rapauth.dto.auth.TimeoutRequest;
import com.rapauth.rapauth.dto.auth.TimeoutResponse;
import com.rapauth.rapauth.dto.auth.VerifyLoginRequest;
import com.rapauth.rapauth.dto.auth.VerifyLoginResponse;

public interface AuthService {

    LoginResponse initiateLogin(LoginRequest request);

    VerifyLoginResponse verifyLogin(VerifyLoginRequest request);

    RegenerateResponse regenerateAuthentication(RegenerateRequest request);

    TimeoutResponse handleTimeout(TimeoutRequest request);
}