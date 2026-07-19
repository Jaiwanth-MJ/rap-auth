package com.rapauth.rapauth.service;

import com.rapauth.rapauth.dto.auth.LoginRequest;
import com.rapauth.rapauth.dto.auth.LoginResponse;
import com.rapauth.rapauth.dto.auth.TimeoutRequest;
import com.rapauth.rapauth.dto.auth.TimeoutResponse;
import com.rapauth.rapauth.dto.auth.VerifyLoginRequest;
import com.rapauth.rapauth.dto.auth.VerifyLoginResponse;
import com.rapauth.rapauth.entity.auth.LoginSession;
import com.rapauth.rapauth.entity.user.User;
import com.rapauth.rapauth.enums.LoginSessionStatus;
import com.rapauth.rapauth.exception.LockedException;
import com.rapauth.rapauth.repository.LoginSessionRepository;
import com.rapauth.rapauth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthServiceLockoutTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginSessionRepository loginSessionRepository;

    private User testUser;

    @BeforeEach
    public void setUp() {
        loginSessionRepository.deleteAll();
        testUser = userRepository.findByRegisterNumber("REG123")
                .orElseThrow(() -> new RuntimeException("Test user 'REG123' must be seeded."));
    }

    private LoginRequest createLoginRequest(String registerNumber) {
        LoginRequest req = new LoginRequest();
        req.setRegisterNumber(registerNumber);
        return req;
    }

    private VerifyLoginRequest createVerifyRequest(Long sessionId, String credential) {
        VerifyLoginRequest req = new VerifyLoginRequest();
        req.setSessionId(sessionId);
        req.setCredential(credential);
        return req;
    }

    private LoginSession createLockedSession(int minutesInPast) {
        LoginSession lockedSession = new LoginSession();
        lockedSession.setUser(testUser);
        lockedSession.setSelectedMethod(com.rapauth.rapauth.entity.auth.AuthenticationMethod.PASSWORD);
        lockedSession.setStatus(LoginSessionStatus.EXPIRED);
        lockedSession.setFailedAttempts(3);
        lockedSession.setRegenerationCount(0);
        lockedSession.setExpiresAt(LocalDateTime.now().plusSeconds(60));
        lockedSession.setLockedUntil(LocalDateTime.now().minusMinutes(minutesInPast));
        return lockedSession;
    }

    @Test
    public void testFirstLockoutDurationIs5Minutes() {
        // 1. Initiate login
        LoginResponse response = authService.initiateLogin(createLoginRequest("REG123"));
        Long sessionId = response.getSessionId();

        // 2. Perform 2 failed attempts
        VerifyLoginRequest failedReq = createVerifyRequest(sessionId, "wrong_password");
        VerifyLoginResponse verifyResp = authService.verifyLogin(failedReq);
        assertFalse(verifyResp.isSuccess());
        assertTrue(verifyResp.getMessage().contains("Remaining attempts: 2"));

        verifyResp = authService.verifyLogin(failedReq);
        assertFalse(verifyResp.isSuccess());
        assertTrue(verifyResp.getMessage().contains("Remaining attempts: 1"));
        assertFalse(verifyResp.getMessage().contains("Warning:"));

        // 3. 3rd failed attempt - should trigger lockout
        verifyResp = authService.verifyLogin(failedReq);
        assertFalse(verifyResp.isSuccess());
        assertTrue(verifyResp.getMessage().contains("locked for 5 minutes"));

        // 4. Try to initiate login again - should throw LockedException
        LockedException ex = assertThrows(LockedException.class, () -> {
            authService.initiateLogin(createLoginRequest("REG123"));
        });
        assertTrue(ex.getMessage().contains("Account is locked"));
        assertTrue(ex.getMessage().contains("5 minutes"));
    }

    @Test
    public void testSecondLockoutDurationIs5Minutes() {
        // Seed 1 previous lockout by saving an expired locked session
        LoginSession lockedSession = createLockedSession(10);
        loginSessionRepository.save(lockedSession);

        // Initiate login (should pass since last lockout has expired)
        LoginResponse response = authService.initiateLogin(createLoginRequest("REG123"));
        Long sessionId = response.getSessionId();

        // Fail 3 times
        VerifyLoginRequest failedReq = createVerifyRequest(sessionId, "wrong_password");
        authService.verifyLogin(failedReq);
        VerifyLoginResponse verifyResp2 = authService.verifyLogin(failedReq);
        assertTrue(verifyResp2.getMessage().contains("Remaining attempts: 1"));
        assertTrue(verifyResp2.getMessage().contains("Warning: The next lockout will be for 5 minutes, but subsequent lockouts will be for 12 hours."));

        VerifyLoginResponse verifyResp = authService.verifyLogin(failedReq);

        // Lockout should now be 5 minutes
        assertTrue(verifyResp.getMessage().contains("locked for 5 minutes"));

        LockedException ex = assertThrows(LockedException.class, () -> {
            authService.initiateLogin(createLoginRequest("REG123"));
        });
        assertTrue(ex.getMessage().contains("5 minutes"));
    }

    @Test
    public void testThirdLockoutDurationIs12Hours() {
        // Seed 2 previous lockouts in the past
        for (int i = 0; i < 2; i++) {
            LoginSession lockedSession = createLockedSession(30);
            loginSessionRepository.save(lockedSession);
        }

        // Initiate login
        LoginResponse response = authService.initiateLogin(createLoginRequest("REG123"));
        Long sessionId = response.getSessionId();

        // Fail 3 times
        VerifyLoginRequest failedReq = createVerifyRequest(sessionId, "wrong_password");
        authService.verifyLogin(failedReq);
        VerifyLoginResponse verifyResp2 = authService.verifyLogin(failedReq);
        assertTrue(verifyResp2.getMessage().contains("Remaining attempts: 1"));
        assertTrue(verifyResp2.getMessage().contains("Warning: The next lockout will be for 12 hours."));

        VerifyLoginResponse verifyResp = authService.verifyLogin(failedReq);

        // Lockout should now be 12 hours
        assertTrue(verifyResp.getMessage().contains("locked for 12 hours"));

        LockedException ex = assertThrows(LockedException.class, () -> {
            authService.initiateLogin(createLoginRequest("REG123"));
        });
        assertTrue(ex.getMessage().contains("12 hours"));
    }

    @Test
    public void testFourthLockoutDurationIs12Hours() {
        // Seed 3 previous lockouts in the past
        for (int i = 0; i < 3; i++) {
            LoginSession lockedSession = createLockedSession(60);
            loginSessionRepository.save(lockedSession);
        }

        // Initiate login
        LoginResponse response = authService.initiateLogin(createLoginRequest("REG123"));
        Long sessionId = response.getSessionId();

        // Fail 3 times
        VerifyLoginRequest failedReq = createVerifyRequest(sessionId, "wrong_password");
        authService.verifyLogin(failedReq);
        VerifyLoginResponse verifyResp2 = authService.verifyLogin(failedReq);
        assertTrue(verifyResp2.getMessage().contains("Remaining attempts: 1"));
        assertTrue(verifyResp2.getMessage().contains("Warning: The next lockout will be for 12 hours."));

        VerifyLoginResponse verifyResp = authService.verifyLogin(failedReq);

        // Lockout should now be 12 hours
        assertTrue(verifyResp.getMessage().contains("locked for 12 hours"));

        LockedException ex = assertThrows(LockedException.class, () -> {
            authService.initiateLogin(createLoginRequest("REG123"));
        });
        assertTrue(ex.getMessage().contains("12 hours"));
    }

    @Test
    public void testTimeoutRotation() {
        // 1. Initiate login
        LoginResponse response = authService.initiateLogin(createLoginRequest("REG123"));
        Long sessionId = response.getSessionId();

        // 2. Call handleTimeout for the 1st time -> should rotate, not lock
        TimeoutRequest timeoutReq = new TimeoutRequest();
        timeoutReq.setSessionId(sessionId);

        TimeoutResponse timeoutResp = authService.handleTimeout(timeoutReq);
        assertFalse(timeoutResp.isLocked());
        assertNotNull(timeoutResp.getAuthenticationMethod());
        assertEquals(1, timeoutResp.getRegenerationCount());
        assertEquals(60, timeoutResp.getExpiresInSeconds());
    }

    @Test
    public void testTimeoutLockout() {
        // 1. Initiate login
        LoginResponse response = authService.initiateLogin(createLoginRequest("REG123"));
        Long sessionId = response.getSessionId();

        // 2. Timeout 1 -> rotate
        TimeoutRequest timeoutReq = new TimeoutRequest();
        timeoutReq.setSessionId(sessionId);
        TimeoutResponse timeoutResp1 = authService.handleTimeout(timeoutReq);
        assertFalse(timeoutResp1.isLocked());

        // 3. Timeout 2 -> lockout (5 mins since lockoutCount == 0)
        TimeoutResponse timeoutResp2 = authService.handleTimeout(timeoutReq);
        assertTrue(timeoutResp2.isLocked());
        assertTrue(timeoutResp2.getMessage().contains("locked for 5 minutes"));

        // 4. Try to login again -> should throw LockedException
        LockedException ex = assertThrows(LockedException.class, () -> {
            authService.initiateLogin(createLoginRequest("REG123"));
        });
        assertTrue(ex.getMessage().contains("5 minutes"));
    }
}
