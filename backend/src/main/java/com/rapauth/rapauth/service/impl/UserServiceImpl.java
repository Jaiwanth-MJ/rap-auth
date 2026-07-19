package com.rapauth.rapauth.service.impl;

import com.rapauth.rapauth.dto.user.UserRequest;
import com.rapauth.rapauth.dto.user.UserResponse;
import com.rapauth.rapauth.entity.auth.AuthenticationEnrollment;
import com.rapauth.rapauth.entity.auth.AuthenticationMethod;
import com.rapauth.rapauth.entity.user.User;
import com.rapauth.rapauth.exception.ResourceAlreadyExistsException;
import com.rapauth.rapauth.repository.AuthenticationEnrollmentRepository;
import com.rapauth.rapauth.repository.UserRepository;
import com.rapauth.rapauth.service.AuthenticationCredentialService;
import com.rapauth.rapauth.service.AuthenticationEnrollmentService;
import com.rapauth.rapauth.service.UserService;
import com.rapauth.rapauth.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationEnrollmentService authenticationEnrollmentService;
    private final AuthenticationCredentialService authenticationCredentialService;
    private final AuthenticationEnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public UserResponse createUser(UserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByRegisterNumber(request.getRegisterNumber())) {
            throw new ResourceAlreadyExistsException("Register number already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRegisterNumber(request.getRegisterNumber());
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        // Create authentication enrollments
        authenticationEnrollmentService.enrollMethods(
                savedUser,
                request.getAuthenticationMethods()
        );

        // Save credentials for only the enrolled methods
        for (AuthenticationMethod method : request.getAuthenticationMethods()) {
            if (method == AuthenticationMethod.PASSWORD) {
                authenticationCredentialService.savePasswordCredential(
                        savedUser.getId(),
                        request.getPassword()
                );
            } else {
                AuthenticationEnrollment enrollment = enrollmentRepository
                        .findByUserAndMethod(savedUser, method)
                        .orElseThrow(() -> new RuntimeException("Enrollment not found for method: " + method));

                String rawCredential;
                if (method == AuthenticationMethod.PASSKEY) {
                    rawCredential = request.getRegisterNumber() + "-passkey";
                } else if (method == AuthenticationMethod.FINGERPRINT) {
                    rawCredential = request.getRegisterNumber() + "-fingerprint";
                } else { // AUTHENTICATOR
                    rawCredential = "123456";
                }

                authenticationCredentialService.saveCredential(
                        enrollment,
                        passwordEncoder.encode(rawCredential),
                        null
                );
            }
        }

        return UserMapper.toResponse(savedUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByRegisterNumber(String registerNumber) {
        return userRepository.findByRegisterNumber(registerNumber);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
public Optional<User> findById(Long id) {
    return userRepository.findById(id);
}
}