package com.rapauth.rapauth.service.impl;

import com.rapauth.rapauth.entity.auth.AuthenticationEnrollment;
import com.rapauth.rapauth.entity.auth.LoginSession;
import com.rapauth.rapauth.entity.user.User;
import com.rapauth.rapauth.enums.LoginSessionStatus;
import com.rapauth.rapauth.exception.ResourceNotFoundException;
import com.rapauth.rapauth.repository.AuthenticationEnrollmentRepository;
import com.rapauth.rapauth.repository.LoginSessionRepository;
import com.rapauth.rapauth.service.RapEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RapEngineServiceImpl implements RapEngineService {

    private final AuthenticationEnrollmentRepository enrollmentRepository;
    private final LoginSessionRepository loginSessionRepository;

    private final Random random = new Random();

    @Override
    public LoginSession createLoginSession(User user) {

        List<AuthenticationEnrollment> enrollments =
                enrollmentRepository.findByUserAndEnabledTrue(user);

        if (enrollments.isEmpty()) {
            throw new RuntimeException("No authentication methods enrolled.");
        }

        AuthenticationEnrollment selected =
                enrollments.get(random.nextInt(enrollments.size()));

        LoginSession session = new LoginSession();
        session.setUser(user);
        session.setSelectedMethod(selected.getMethod());
        session.setStatus(LoginSessionStatus.PENDING);
        session.setRegenerationCount(0);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(60));

        Optional<LoginSession> lastSessionOpt = loginSessionRepository.findFirstByUserOrderByCreatedAtDesc(user);
        int inheritedFailedAttempts = 0;
        if (lastSessionOpt.isPresent()) {
            LoginSession lastSession = lastSessionOpt.get();
            if (lastSession.getLockedUntil() == null) {
                if (lastSession.getUpdatedAt() != null &&
                        lastSession.getUpdatedAt().isBefore(LocalDateTime.now().minusMinutes(15))) {
                    inheritedFailedAttempts = 0;
                } else {
                    inheritedFailedAttempts = lastSession.getFailedAttempts();
                }
            }
        }
        session.setFailedAttempts(inheritedFailedAttempts);

        return loginSessionRepository.save(session);
    }

    @Override
    public LoginSession regenerateAuthentication(Long sessionId) {

        LoginSession session = loginSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Login session not found"));

        if (session.getStatus() != LoginSessionStatus.PENDING) {
            throw new RuntimeException("Login session is no longer active.");
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(LoginSessionStatus.EXPIRED);
            loginSessionRepository.save(session);
            throw new RuntimeException("Login session expired.");
        }

        if (session.getRegenerationCount() >= 2) {
            throw new RuntimeException("Maximum regeneration limit reached.");
        }

        List<AuthenticationEnrollment> availableMethods =
                enrollmentRepository.findByUserAndEnabledTrue(session.getUser())
                        .stream()
                        .filter(enrollment ->
                                enrollment.getMethod() != session.getSelectedMethod())
                        .collect(Collectors.toList());

        if (availableMethods.isEmpty()) {
            throw new RuntimeException("No alternate authentication methods available.");
        }

        AuthenticationEnrollment selected =
                availableMethods.get(random.nextInt(availableMethods.size()));

        session.setSelectedMethod(selected.getMethod());
        session.setRegenerationCount(session.getRegenerationCount() + 1);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(60));

        return loginSessionRepository.save(session);
    }
}