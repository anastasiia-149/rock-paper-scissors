package com.techub.rps.boundary.outgoing;

import com.techub.rps.boundary.outgoing.db.UserEntity;
import com.techub.rps.boundary.outgoing.db.UserRepository;
import com.techub.rps.control.model.User;
import com.techub.rps.control.ports.UserRegistrationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationAdapter implements UserRegistrationPort {

    private final UserRepository userRepository;

    @Override
    public User registerUser(String username) {
        log.info("Registering user in database: {}", username);

        UserEntity userEntity = UserEntity.builder()
                .username(username)
                .build();

        UserEntity savedEntity = userRepository.save(userEntity);

        return User.builder()
                .username(savedEntity.getUsername())
                .createdAt(savedEntity.getCreatedAt())
                .build();
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
