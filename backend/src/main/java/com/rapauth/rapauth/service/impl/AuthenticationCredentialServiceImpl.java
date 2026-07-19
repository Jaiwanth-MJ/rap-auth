package com.rapauth.rapauth.service.impl;

import com.rapauth.rapauth.entity.auth.AuthenticationCredential;
import com.rapauth.rapauth.entity.auth.AuthenticationEnrollment;
import com.rapauth.rapauth.entity.auth.AuthenticationMethod;
import com.rapauth.rapauth.entity.user.User;
import com.rapauth.rapauth.repository.AuthenticationCredentialRepository;
import com.rapauth.rapauth.repository.AuthenticationEnrollmentRepository;
import com.rapauth.rapauth.repository.UserRepository;
import com.rapauth.rapauth.service.AuthenticationCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationCredentialServiceImpl
        implements AuthenticationCredentialService {

    private final AuthenticationCredentialRepository credentialRepository;
    private final AuthenticationEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthenticationCredential saveCredential(
            AuthenticationEnrollment enrollment,
            String credentialValue,
            String metadata
    ) {

        AuthenticationCredential credential = new AuthenticationCredential();
        credential.setAuthenticationEnrollment(enrollment);
        credential.setCredentialValue(credentialValue);
        credential.setMetadata(metadata);

        return credentialRepository.save(credential);
    }

    @Override
    public AuthenticationCredential getCredential(
            AuthenticationEnrollment enrollment
    ) {
        return credentialRepository
                .findByAuthenticationEnrollment(enrollment)
                .orElseThrow(() -> new RuntimeException("Credential not found"));
    }

    @Override
    public AuthenticationCredential savePasswordCredential(
            Long userId,
            String rawPassword
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthenticationEnrollment enrollment = enrollmentRepository
                .findByUserAndEnabledTrue(user)
                .stream()
                .filter(e -> e.getMethod() == AuthenticationMethod.PASSWORD)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Password enrollment not found"));

        String hashedPassword = passwordEncoder.encode(rawPassword);

        return saveCredential(enrollment, hashedPassword, null);
    }
}