package com.techub.rps.control.ports;

import com.techub.rps.control.exception.DomainException;
import com.techub.rps.control.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRegistrationService Tests")
class UserRegistrationServiceTest {

    @Mock
    private UserRegistrationPort userRegistrationPort;

    @Mock
    private UserStatisticsPort userStatisticsPort;

    private UserRegistrationService userRegistrationService;

    private static final String VALID_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        userRegistrationService = new UserRegistrationService(userRegistrationPort, userStatisticsPort);
    }

    @Test
    @DisplayName("registerUser should create user and initialize statistics when username is valid and unique")
    void registerUser_shouldCreateUser_whenUsernameIsValidAndUnique() {
        User expectedUser = User.builder()
                .username(VALID_USERNAME)
                .createdAt(Instant.now())
                .build();

        when(userRegistrationPort.usernameExists(VALID_USERNAME)).thenReturn(false);
        when(userRegistrationPort.registerUser(VALID_USERNAME)).thenReturn(expectedUser);

        User result = userRegistrationService.registerUser(VALID_USERNAME);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(VALID_USERNAME);
        assertThat(result.getCreatedAt()).isNotNull();

        verify(userRegistrationPort, times(1)).usernameExists(VALID_USERNAME);
        verify(userRegistrationPort, times(1)).registerUser(VALID_USERNAME);
        verify(userStatisticsPort, times(1)).initializeStatistics(VALID_USERNAME);
    }

    @Test
    @DisplayName("registerUser should throw DomainException when username is null")
    void registerUser_shouldThrowDomainException_whenUsernameIsNull() {
        assertThatThrownBy(() -> userRegistrationService.registerUser(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Username cannot be null or empty")
                .extracting("errorCode").isEqualTo("INVALID_USERNAME");

        verify(userRegistrationPort, never()).usernameExists(any());
        verify(userRegistrationPort, never()).registerUser(any());
        verify(userStatisticsPort, never()).initializeStatistics(any());
    }

    @Test
    @DisplayName("registerUser should throw DomainException when username is empty")
    void registerUser_shouldThrowDomainException_whenUsernameIsEmpty() {
        assertThatThrownBy(() -> userRegistrationService.registerUser(""))
                .isInstanceOf(DomainException.class)
                .hasMessage("Username cannot be null or empty")
                .extracting("errorCode").isEqualTo("INVALID_USERNAME");

        verify(userRegistrationPort, never()).usernameExists(any());
        verify(userRegistrationPort, never()).registerUser(any());
        verify(userStatisticsPort, never()).initializeStatistics(any());
    }

    @Test
    @DisplayName("registerUser should throw DomainException when username is only whitespace")
    void registerUser_shouldThrowDomainException_whenUsernameIsOnlyWhitespace() {
        assertThatThrownBy(() -> userRegistrationService.registerUser("   "))
                .isInstanceOf(DomainException.class)
                .hasMessage("Username cannot be null or empty")
                .extracting("errorCode").isEqualTo("INVALID_USERNAME");

        verify(userRegistrationPort, never()).usernameExists(any());
        verify(userRegistrationPort, never()).registerUser(any());
        verify(userStatisticsPort, never()).initializeStatistics(any());
    }

    @Test
    @DisplayName("registerUser should throw DomainException when username is too short")
    void registerUser_shouldThrowDomainException_whenUsernameIsTooShort() {
        assertThatThrownBy(() -> userRegistrationService.registerUser("ab"))
                .isInstanceOf(DomainException.class)
                .hasMessage("Username must be between 3 and 50 characters")
                .extracting("errorCode").isEqualTo("INVALID_USERNAME");

        verify(userRegistrationPort, never()).usernameExists(any());
        verify(userRegistrationPort, never()).registerUser(any());
        verify(userStatisticsPort, never()).initializeStatistics(any());
    }

    @Test
    @DisplayName("registerUser should throw DomainException when username is too long")
    void registerUser_shouldThrowDomainException_whenUsernameIsTooLong() {
        String longUsername = "a".repeat(51);

        assertThatThrownBy(() -> userRegistrationService.registerUser(longUsername))
                .isInstanceOf(DomainException.class)
                .hasMessage("Username must be between 3 and 50 characters")
                .extracting("errorCode").isEqualTo("INVALID_USERNAME");

        verify(userRegistrationPort, never()).usernameExists(any());
        verify(userRegistrationPort, never()).registerUser(any());
        verify(userStatisticsPort, never()).initializeStatistics(any());
    }

    @Test
    @DisplayName("registerUser should throw DomainException when username already exists")
    void registerUser_shouldThrowDomainException_whenUsernameAlreadyExists() {
        when(userRegistrationPort.usernameExists(VALID_USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> userRegistrationService.registerUser(VALID_USERNAME))
                .isInstanceOf(DomainException.class)
                .hasMessage("Username already exists: " + VALID_USERNAME)
                .extracting("errorCode").isEqualTo("INVALID_USERNAME");

        verify(userRegistrationPort, times(1)).usernameExists(VALID_USERNAME);
        verify(userRegistrationPort, never()).registerUser(any());
        verify(userStatisticsPort, never()).initializeStatistics(any());
    }

    @Test
    @DisplayName("registerUser should accept username with exactly 3 characters")
    void registerUser_shouldAcceptUsername_withExactly3Characters() {
        String minUsername = "abc";
        User expectedUser = User.builder()
                .username(minUsername)
                .createdAt(Instant.now())
                .build();

        when(userRegistrationPort.usernameExists(minUsername)).thenReturn(false);
        when(userRegistrationPort.registerUser(minUsername)).thenReturn(expectedUser);

        User result = userRegistrationService.registerUser(minUsername);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(minUsername);

        verify(userRegistrationPort, times(1)).usernameExists(minUsername);
        verify(userRegistrationPort, times(1)).registerUser(minUsername);
        verify(userStatisticsPort, times(1)).initializeStatistics(minUsername);
    }

    @Test
    @DisplayName("registerUser should accept username with exactly 50 characters")
    void registerUser_shouldAcceptUsername_withExactly50Characters() {
        String maxUsername = "a".repeat(50);
        User expectedUser = User.builder()
                .username(maxUsername)
                .createdAt(Instant.now())
                .build();

        when(userRegistrationPort.usernameExists(maxUsername)).thenReturn(false);
        when(userRegistrationPort.registerUser(maxUsername)).thenReturn(expectedUser);

        User result = userRegistrationService.registerUser(maxUsername);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(maxUsername);

        verify(userRegistrationPort, times(1)).usernameExists(maxUsername);
        verify(userRegistrationPort, times(1)).registerUser(maxUsername);
        verify(userStatisticsPort, times(1)).initializeStatistics(maxUsername);
    }
}
