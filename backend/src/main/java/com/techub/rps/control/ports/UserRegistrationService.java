package com.techub.rps.control.ports;

import com.techub.rps.control.exception.DomainException;
import com.techub.rps.control.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {

    private final UserRegistrationPort userRegistrationPort;
    private final UserStatisticsPort userStatisticsPort;

    public User registerUser(String username) {
        log.info("Registering user: {}", username);

        validateUsername(username);

        if (userRegistrationPort.usernameExists(username)) {
            log.warn("Username already exists: {}", username);
            throw DomainException.invalidUsername("Username already exists: " + username);
        }

        User user = userRegistrationPort.registerUser(username);
        userStatisticsPort.initializeStatistics(username);
        log.info("User registered successfully: {}", username);

        return user;
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw DomainException.invalidUsername("Username cannot be null or empty");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw DomainException.invalidUsername("Username must be between 3 and 50 characters");
        }
    }
}

