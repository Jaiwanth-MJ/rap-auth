package com.rapauth.rapauth.repository;

import com.rapauth.rapauth.entity.auth.AuthenticationEnrollment;
import com.rapauth.rapauth.entity.auth.AuthenticationMethod;
import com.rapauth.rapauth.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthenticationEnrollmentRepository
        extends JpaRepository<AuthenticationEnrollment, Long> {

    List<AuthenticationEnrollment> findByUserAndEnabledTrue(User user);

    Optional<AuthenticationEnrollment> findByUserAndMethod(
            User user,
            AuthenticationMethod method
    );
}