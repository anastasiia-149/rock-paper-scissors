package com.techub.rps.boundary.incoming;

import com.techub.rps.boundary.incoming.dto.ErrorResponse;
import com.techub.rps.control.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleDomainException should return 400 BAD_REQUEST for client errors")
    void handleDomainException_shouldReturnBadRequestForClientErrors() {
        // given
        DomainException exception = DomainException.invalidHand("Invalid hand");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDomainException(exception);

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(400),
                () -> assertThat(Objects.requireNonNull(response.getBody()).getMessage()).contains("Invalid hand"),
                () -> assertThat(Objects.requireNonNull(response.getBody()).getErrorCode()).isEqualTo("INVALID_HAND"),
                () -> assertThat(Objects.requireNonNull(response.getBody()).getTimestamp()).isNotNull()
        );
    }

    @Test
    @DisplayName("handleDomainException should return 500 INTERNAL_SERVER_ERROR for random generation errors")
    void handleDomainException_shouldReturnInternalServerErrorForRandomGeneration() {
        // given
        DomainException exception = DomainException.randomGenerationError("Random generation failed");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDomainException(exception);

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(500),
                () -> assertThat(Objects.requireNonNull(response.getBody()).getMessage()).contains("Random generation failed"),
                () -> assertThat(Objects.requireNonNull(response.getBody()).getErrorCode()).isEqualTo("RANDOM_GENERATION_ERROR"),
                () -> assertThat(Objects.requireNonNull(response.getBody()).getTimestamp()).isNotNull()
        );
    }

    @Test
    @DisplayName("handleDomainException should return 500 INTERNAL_SERVER_ERROR for game errors")
    void handleDomainException_shouldReturnInternalServerErrorForGameErrors() {
        // given
        DomainException exception = DomainException.gameError("Game error occurred", new RuntimeException("cause"));

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDomainException(exception);

        // then
        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(500),
                () -> assertThat(Objects.requireNonNull(response.getBody()).getMessage()).contains("Game error occurred"),
                () -> assertThat(Objects.requireNonNull(response.getBody()).getErrorCode()).isEqualTo("GAME_ERROR"),
                () -> assertThat(Objects.requireNonNull(response.getBody()).getTimestamp()).isNotNull()
        );
    }

    @Test
    @DisplayName("handleDomainException should handle nested exceptions")
    void handleDomainException_shouldHandleNestedException() {
        // given
        RuntimeException cause = new RuntimeException("Root cause");
        DomainException exception = DomainException.gameError("Failed to play game: Root cause", cause);

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDomainException(exception);

        // then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Failed to play game");
    }

    @Test
    @DisplayName("Error response should have consistent structure")
    void errorResponse_shouldHaveConsistentStructure() {
        // given
        DomainException exception = DomainException.invalidHand("Test error");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDomainException(exception);
        ErrorResponse errorResponse = response.getBody();

        // then
        assertAll(
                () -> {
                    assert errorResponse != null;
                    assertThat(errorResponse.getStatus()).isNotNull();
                    assertThat(errorResponse.getErrorCode()).isNotNull();
                    assertThat(errorResponse.getMessage()).isNotNull();
                    assertThat(errorResponse.getTimestamp()).isNotNull();
                }
        );
    }

    @Test
    @DisplayName("All domain exceptions should return appropriate status codes")
    void allDomainExceptions_shouldReturnAppropriateStatusCodes() {
        // given
        DomainException invalidHandEx = DomainException.invalidHand("Invalid hand");
        DomainException randomEx = DomainException.randomGenerationError("Random error");
        DomainException gameEx = DomainException.gameError("Game error", new RuntimeException());

        // when
        ResponseEntity<ErrorResponse> invalidHandResponse = exceptionHandler.handleDomainException(invalidHandEx);
        ResponseEntity<ErrorResponse> randomResponse = exceptionHandler.handleDomainException(randomEx);
        ResponseEntity<ErrorResponse> gameResponse = exceptionHandler.handleDomainException(gameEx);

        // then
        assertAll(
                () -> assertThat(invalidHandResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                () -> assertThat(randomResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR),
                () -> assertThat(gameResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }
}
