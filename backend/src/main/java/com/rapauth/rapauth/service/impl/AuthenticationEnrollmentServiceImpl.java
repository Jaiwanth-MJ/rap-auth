package com.rapauth.rapauth.service.impl;

import com.rapauth.rapauth.entity.auth.AuthenticationEnrollment;
import com.rapauth.rapauth.entity.auth.AuthenticationMethod;
import com.rapauth.rapauth.entity.user.User;
import com.rapauth.rapauth.repository.AuthenticationEnrollmentRepository;
import com.rapauth.rapauth.service.AuthenticationEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationEnrollmentServiceImpl
        implements AuthenticationEnrollmentService {

    private final AuthenticationEnrollmentRepository enrollmentRepository;

    @Override
    public void enrollMethods(User user, List<AuthenticationMethod> methods) {

        for (AuthenticationMethod method : methods) {

            AuthenticationEnrollment enrollment = new AuthenticationEnrollment();
            enrollment.setUser(user);
            enrollment.setMethod(method);
            enrollment.setEnabled(true);

            enrollmentRepository.save(enrollment);
        }
    }
}