package com.rapauth.rapauth.repository;

import com.rapauth.rapauth.entity.auth.LoginSession;
import com.rapauth.rapauth.entity.user.User;
import com.rapauth.rapauth.enums.LoginSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LoginSessionRepository extends JpaRepository<LoginSession, Long> {

    Optional<LoginSession> findByIdAndStatus(Long id, LoginSessionStatus status);

    Optional<LoginSession> findFirstByUserOrderByCreatedAtDesc(User user);

    Optional<LoginSession> findFirstByUserAndStatusOrderByCreatedAtDesc(User user, LoginSessionStatus status);

    long countByUserAndLockedUntilIsNotNullAndCreatedAtAfter(User user, LocalDateTime dateTime);

    long countByUserAndLockedUntilIsNotNull(User user);

}