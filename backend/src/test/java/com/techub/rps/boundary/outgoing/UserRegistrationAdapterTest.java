package com.techub.rps.boundary.outgoing;

import com.techub.rps.boundary.outgoing.db.UserEntity;
import com.techub.rps.boundary.outgoing.db.UserRepository;
import com.techub.rps.control.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRegistrationAdapter Tests")
class UserRegistrationAdapterTest {

    @Mock
    private UserRepository userRepository;

    private UserRegistrationAdapter userRegistrationAdapter;

    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        userRegistrationAdapter = new UserRegistrationAdapter(userRepository);
    }

    @Test
    @DisplayName("registerUser should save user and return domain model")
    void registerUser_shouldSaveUserAndReturnDomainModel() {
        Instant now = Instant.now();
        UserEntity savedEntity = UserEntity.builder()
                .username(TEST_USERNAME)
                .createdAt(now)
                .build();

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);
        User result = userRegistrationAdapter.registerUser(TEST_USERNAME);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("registerUser should create entity with correct username")
    void registerUser_shouldCreateEntityWithCorrectUsername() {
        UserEntity capturedEntity = UserEntity.builder()
                .username(TEST_USERNAME)
                .createdAt(Instant.now())
                .build();

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            assertThat(entity.getUsername()).isEqualTo(TEST_USERNAME);
            return capturedEntity;
        });

        userRegistrationAdapter.registerUser(TEST_USERNAME);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("usernameExists should return true when username exists")
    void usernameExists_shouldReturnTrue_whenUsernameExists() {
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        boolean result = userRegistrationAdapter.usernameExists(TEST_USERNAME);

        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("usernameExists should return false when username does not exist")
    void usernameExists_shouldReturnFalse_whenUsernameDoesNotExist() {
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);

        boolean result = userRegistrationAdapter.usernameExists(TEST_USERNAME);

        assertThat(result).isFalse();
        verify(userRepository, times(1)).existsByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("registerUser should map entity to domain model correctly")
    void registerUser_shouldMapEntityToDomainModelCorrectly() {
        Instant createdAt = Instant.parse("2025-12-10T10:00:00Z");
        UserEntity entity = UserEntity.builder()
                .username(TEST_USERNAME)
                .createdAt(createdAt)
                .build();

        when(userRepository.save(any(UserEntity.class))).thenReturn(entity);

        User result = userRegistrationAdapter.registerUser(TEST_USERNAME);

        assertThat(result.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
    }
}
