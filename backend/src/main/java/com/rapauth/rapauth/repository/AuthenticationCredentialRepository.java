package com.rapauth.rapauth.repository;

import com.rapauth.rapauth.entity.auth.AuthenticationCredential;
import com.rapauth.rapauth.entity.auth.AuthenticationEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthenticationCredentialRepository
        extends JpaRepository<AuthenticationCredential, Long> {

    Optional<AuthenticationCredential> findByAuthenticationEnrollment(
            AuthenticationEnrollment authenticationEnrollment
    );
}