package com.rapauth.rapauth.service.impl;

import com.rapauth.rapauth.dto.auth.LoginRequest;
import com.rapauth.rapauth.dto.auth.LoginResponse;
import com.rapauth.rapauth.dto.auth.RegenerateRequest;
import com.rapauth.rapauth.dto.auth.RegenerateResponse;
import com.rapauth.rapauth.dto.auth.TimeoutRequest;
import com.rapauth.rapauth.dto.auth.TimeoutResponse;
import com.rapauth.rapauth.dto.auth.VerifyLoginRequest;
import com.rapauth.rapauth.dto.auth.VerifyLoginResponse;
import com.rapauth.rapauth.entity.auth.AuthenticationCredential;
import com.rapauth.rapauth.entity.auth.AuthenticationEnrollment;
import com.rapauth.rapauth.entity.auth.LoginSession;
import com.rapauth.rapauth.entity.user.User;
import com.rapauth.rapauth.enums.LoginSessionStatus;
import com.rapauth.rapauth.exception.LockedException;
import com.rapauth.rapauth.exception.ResourceNotFoundException;
import com.rapauth.rapauth.repository.AuthenticationCredentialRepository;
import com.rapauth.rapauth.repository.AuthenticationEnrollmentRepository;
import com.rapauth.rapauth.repository.LoginSessionRepository;
import com.rapauth.rapauth.security.JwtService;
import com.rapauth.rapauth.service.AuthService;
import com.rapauth.rapauth.service.RapEngineService;
import com.rapauth.rapauth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final RapEngineService rapEngineService;
    private final LoginSessionRepository loginSessionRepository;
    private final AuthenticationEnrollmentRepository authenticationEnrollmentRepository;
    private final AuthenticationCredentialRepository authenticationCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse initiateLogin(LoginRequest request) {

        User user = userService.findByRegisterNumber(request.getRegisterNumber())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<LoginSession> lastSessionOpt = loginSessionRepository.findFirstByUserOrderByCreatedAtDesc(user);
        if (lastSessionOpt.isPresent()) {
            LoginSession lastSession = lastSessionOpt.get();
            if (lastSession.getLockedUntil() != null && lastSession.getLockedUntil().isAfter(LocalDateTime.now())) {
                long secondsLeft = java.time.Duration.between(LocalDateTime.now(), lastSession.getLockedUntil()).toSeconds();
                String timeString;
                if (secondsLeft >= 3600) {
                    long hours = (secondsLeft + 3599) / 3600;
                    timeString = hours + (hours == 1 ? " hour" : " hours");
                } else {
                    long minutes = (secondsLeft + 59) / 60;
                    timeString = minutes + (minutes == 1 ? " minute" : " minutes");
                }
                throw new LockedException("Account is locked. Please try again after " + timeString + ".");
            }
        }

        LoginSession session = rapEngineService.createLoginSession(user);

        return LoginResponse.builder()
                .sessionId(session.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .registerNumber(user.getRegisterNumber())
                .authenticationMethod(session.getSelectedMethod().name())
                .expiresInSeconds(60)
                .build();
    }

    @Override
    public VerifyLoginResponse verifyLogin(VerifyLoginRequest request) {

        LoginSession session = loginSessionRepository
                .findByIdAndStatus(request.getSessionId(), LoginSessionStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid login session"));

        if (session.getLockedUntil() != null &&
                session.getLockedUntil().isAfter(LocalDateTime.now())) {

            return VerifyLoginResponse.builder()
                    .success(false)
                    .message("Authentication is locked until " + session.getLockedUntil())
                    .token(null)
                    .build();
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(LoginSessionStatus.EXPIRED);
            loginSessionRepository.save(session);

            return VerifyLoginResponse.builder()
                    .success(false)
                    .message("Login session expired")
                    .token(null)
                    .build();
        }

        AuthenticationEnrollment enrollment = authenticationEnrollmentRepository
                .findByUserAndMethod(session.getUser(), session.getSelectedMethod())
                .orElseThrow(() -> new ResourceNotFoundException("Authentication enrollment not found"));

        AuthenticationCredential credential = authenticationCredentialRepository
                .findByAuthenticationEnrollment(enrollment)
                .orElseThrow(() -> new ResourceNotFoundException("Authentication credential not found"));

        if (!passwordEncoder.matches(
                request.getCredential(),
                credential.getCredentialValue())) {

            session.setFailedAttempts(session.getFailedAttempts() + 1);

            Optional<LoginSession> lastVerifiedSessionOpt = loginSessionRepository
                    .findFirstByUserAndStatusOrderByCreatedAtDesc(session.getUser(), LoginSessionStatus.VERIFIED);

            long lockoutCount;
            if (lastVerifiedSessionOpt.isPresent()) {
                lockoutCount = loginSessionRepository.countByUserAndLockedUntilIsNotNullAndCreatedAtAfter(
                        session.getUser(), lastVerifiedSessionOpt.get().getCreatedAt());
            } else {
                lockoutCount = loginSessionRepository.countByUserAndLockedUntilIsNotNull(session.getUser());
            }

            if (session.getFailedAttempts() >= 3) {
                int minutes;
                String timeMsg;
                if (lockoutCount == 0 || lockoutCount == 1) {
                    minutes = 5;
                    timeMsg = "5 minutes";
                } else {
                    minutes = 720; // 12 hours
                    timeMsg = "12 hours";
                }

                session.setLockedUntil(LocalDateTime.now().plusMinutes(minutes));
                session.setStatus(LoginSessionStatus.EXPIRED);

                loginSessionRepository.save(session);

                return VerifyLoginResponse.builder()
                        .success(false)
                        .message("Authentication locked for " + timeMsg + ".")
                        .token(null)
                        .build();
            }

            loginSessionRepository.save(session);

            String message = "Invalid credential. Remaining attempts: " + (3 - session.getFailedAttempts());
            if (session.getFailedAttempts() == 2) {
                if (lockoutCount == 1) {
                    message += ". Warning: The next lockout will be for 5 minutes, but subsequent lockouts will be for 12 hours.";
                } else if (lockoutCount >= 2) {
                    message += ". Warning: The next lockout will be for 12 hours.";
                }
            }

            return VerifyLoginResponse.builder()
                    .success(false)
                    .message(message)
                    .token(null)
                    .build();
        }

        session.setFailedAttempts(0);
        session.setLockedUntil(null);
        session.setStatus(LoginSessionStatus.VERIFIED);

        loginSessionRepository.save(session);

        String token = jwtService.generateToken(session.getUser());

        return VerifyLoginResponse.builder()
                .success(true)
                .message("Authentication successful")
                .token(token)
                .build();
    }

    @Override
    public RegenerateResponse regenerateAuthentication(RegenerateRequest request) {

        LoginSession session =
                rapEngineService.regenerateAuthentication(request.getSessionId());

        return RegenerateResponse.builder()
                .sessionId(session.getId())
                .authenticationMethod(session.getSelectedMethod().name())
                .regenerationCount(session.getRegenerationCount())
                .expiresInSeconds(60)
                .build();
    }

    @Override
    public TimeoutResponse handleTimeout(TimeoutRequest request) {
        LoginSession session = loginSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Login session not found"));

        if (session.getStatus() != LoginSessionStatus.PENDING) {
            throw new RuntimeException("Login session is no longer active");
        }

        if (session.getLockedUntil() != null && session.getLockedUntil().isAfter(LocalDateTime.now())) {
            return TimeoutResponse.builder()
                    .locked(true)
                    .message("Account is locked")
                    .build();
        }

        if (session.getRegenerationCount() < 1) {
            java.util.List<AuthenticationEnrollment> availableMethods =
                    authenticationEnrollmentRepository.findByUserAndEnabledTrue(session.getUser())
                            .stream()
                            .filter(enrollment -> enrollment.getMethod() != session.getSelectedMethod())
                            .collect(java.util.stream.Collectors.toList());

            if (!availableMethods.isEmpty()) {
                AuthenticationEnrollment selected =
                        availableMethods.get(new java.util.Random().nextInt(availableMethods.size()));

                session.setSelectedMethod(selected.getMethod());
                session.setRegenerationCount(session.getRegenerationCount() + 1);
                session.setExpiresAt(LocalDateTime.now().plusSeconds(60));
                loginSessionRepository.save(session);

                return TimeoutResponse.builder()
                        .locked(false)
                        .authenticationMethod(session.getSelectedMethod().name())
                        .regenerationCount(session.getRegenerationCount())
                        .expiresInSeconds(60)
                        .build();
            }
        }

        Optional<LoginSession> lastVerifiedSessionOpt = loginSessionRepository
                .findFirstByUserAndStatusOrderByCreatedAtDesc(session.getUser(), LoginSessionStatus.VERIFIED);

        long lockoutCount;
        if (lastVerifiedSessionOpt.isPresent()) {
            lockoutCount = loginSessionRepository.countByUserAndLockedUntilIsNotNullAndCreatedAtAfter(
                    session.getUser(), lastVerifiedSessionOpt.get().getCreatedAt());
        } else {
            lockoutCount = loginSessionRepository.countByUserAndLockedUntilIsNotNull(session.getUser());
        }

        int minutes;
        String timeMsg;
        if (lockoutCount == 0 || lockoutCount == 1) {
            minutes = 5;
            timeMsg = "5 minutes";
        } else {
            minutes = 720; // 12 hours
            timeMsg = "12 hours";
        }

        session.setLockedUntil(LocalDateTime.now().plusMinutes(minutes));
        session.setStatus(LoginSessionStatus.EXPIRED);
        loginSessionRepository.save(session);

        return TimeoutResponse.builder()
                .locked(true)
                .message("Authentication locked for " + timeMsg + " due to inactivity.")
                .build();
    }
}