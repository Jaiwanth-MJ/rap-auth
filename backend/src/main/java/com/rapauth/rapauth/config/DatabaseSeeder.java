package com.rapauth.rapauth.config;

import com.rapauth.rapauth.dto.user.UserRequest;
import com.rapauth.rapauth.entity.auth.AuthenticationMethod;
import com.rapauth.rapauth.entity.user.UserRole;
import com.rapauth.rapauth.repository.UserRepository;
import com.rapauth.rapauth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Database is empty. Seeding default user...");
            
            UserRequest request = new UserRequest();
            request.setFullName("Demo User");
            request.setEmail("demo@example.com");
            request.setRegisterNumber("REG123");
            request.setPassword("password123");
            request.setRole(UserRole.STUDENT);
            request.setAuthenticationMethods(Arrays.asList(
                    AuthenticationMethod.PASSWORD,
                    AuthenticationMethod.PASSKEY,
                    AuthenticationMethod.FINGERPRINT,
                    AuthenticationMethod.AUTHENTICATOR
            ));

            userService.createUser(request);
            log.info("Default user seeded: REG123 (demo@example.com) with password123. All authentication methods enrolled.");
        } else {
            log.info("Users already exist in database. Skipping seeding.");
        }
    }
}
