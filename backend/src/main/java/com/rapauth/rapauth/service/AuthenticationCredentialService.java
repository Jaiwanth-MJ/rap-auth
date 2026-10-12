package com.rapauth.rapauth.service;

import com.rapauth.rapauth.entity.auth.AuthenticationCredential;
import com.rapauth.rapauth.entity.auth.AuthenticationEnrollment;

public interface AuthenticationCredentialService {

    AuthenticationCredential saveCredential(
            AuthenticationEnrollment enrollment,
            String credentialValue,
            String metadata
    );

    AuthenticationCredential getCredential(
            AuthenticationEnrollment enrollment
    );

    AuthenticationCredential savePasswordCredential(
            Long userId,
            String rawPassword
    );
}