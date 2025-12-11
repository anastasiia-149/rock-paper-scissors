package com.techub.rps.control.ports;

import com.techub.rps.control.exception.DomainException;
import com.techub.rps.control.model.Game;
import com.techub.rps.control.model.GameResult;
import com.techub.rps.control.model.Hand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameService Tests")
class GameServiceTest {

    @Mock
    private RandomHandProvider randomHandProvider;
    @Mock
    private MetricsProvider metricsPort;

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService(randomHandProvider, metricsPort);
    }

    @Test
    @DisplayName("play should create a game when player wins")
    void play_shouldCreateGame_whenPlayerWins() {
        when(randomHandProvider.getRandomHand()).thenReturn(Hand.SCISSORS);

        Game game = gameService.play(Hand.ROCK);

        assertAll(
                () -> {
                    assert game != null;
                    assertThat(game.getGameId()).isNotNull();
                    assertThat(game.getPlayerHand()).isEqualTo(Hand.ROCK);
                    assertThat(game.getComputerHand()).isEqualTo(Hand.SCISSORS);
                    assertThat(game.getResult()).isEqualTo(GameResult.WIN);
                    assertThat(game.getTimestamp()).isNotNull();
                }
        );

        verify(randomHandProvider, times(1)).getRandomHand();
    }

    @Test
    @DisplayName("play should create a game when player loses")
    void play_shouldCreateGame_whenPlayerLoses() {
        when(randomHandProvider.getRandomHand()).thenReturn(Hand.PAPER);

        Game game = gameService.play(Hand.ROCK);

        assertAll(
                () -> {
                    assert game != null;
                    assertThat(game.getPlayerHand()).isEqualTo(Hand.ROCK);
                    assertThat(game.getComputerHand()).isEqualTo(Hand.PAPER);
                    assertThat(game.getResult()).isEqualTo(GameResult.LOSE);
                }
        );

        verify(randomHandProvider, times(1)).getRandomHand();
    }

    @Test
    @DisplayName("play should create a game when result is draw")
    void play_shouldCreateGame_whenDraw() {
        when(randomHandProvider.getRandomHand()).thenReturn(Hand.ROCK);

        Game game = gameService.play(Hand.ROCK);

        assertAll(
                () -> {
                    assert game != null;
                    assertThat(game.getPlayerHand()).isEqualTo(Hand.ROCK);
                    assertThat(game.getComputerHand()).isEqualTo(Hand.ROCK);
                    assertThat(game.getResult()).isEqualTo(GameResult.DRAW);
                }
        );

        verify(randomHandProvider, times(1)).getRandomHand();
    }

    @Test
    @DisplayName("play should work correctly for PAPER player hand")
    void play_shouldWorkCorrectly_forPaper() {
        when(randomHandProvider.getRandomHand()).thenReturn(Hand.ROCK);

        Game game = gameService.play(Hand.PAPER);

        assertAll(
                () -> assertThat(game.getPlayerHand()).isEqualTo(Hand.PAPER),
                () -> assertThat(game.getComputerHand()).isEqualTo(Hand.ROCK),
                () -> assertThat(game.getResult()).isEqualTo(GameResult.WIN)
        );
    }

    @Test
    @DisplayName("play should work correctly for SCISSORS player hand")
    void play_shouldWorkCorrectly_forScissors() {
        when(randomHandProvider.getRandomHand()).thenReturn(Hand.PAPER);

        Game game = gameService.play(Hand.SCISSORS);

        assertAll(
                () -> assertThat(game.getPlayerHand()).isEqualTo(Hand.SCISSORS),
                () -> assertThat(game.getComputerHand()).isEqualTo(Hand.PAPER),
                () -> assertThat(game.getResult()).isEqualTo(GameResult.WIN)
        );
    }

    @Test
    @DisplayName("play should throw DomainException when player hand is null")
    void play_shouldThrowDomainException_whenPlayerHandIsNull() {
        assertThatThrownBy(() -> gameService.play(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Player hand cannot be null")
                .extracting("errorCode").isEqualTo("INVALID_HAND");

        verify(randomHandProvider, never()).getRandomHand();
    }

    @Test
    @DisplayName("play should throw DomainException when RandomHandProvider fails")
    void play_shouldThrowDomainException_whenRandomHandProviderFails() {
        when(randomHandProvider.getRandomHand())
                .thenThrow(DomainException.randomGenerationError("Random generation failed"));

        assertThatThrownBy(() -> gameService.play(Hand.ROCK))
                .isInstanceOf(DomainException.class)
                .extracting("errorCode").isEqualTo("RANDOM_GENERATION_ERROR");

        verify(randomHandProvider, times(1)).getRandomHand();
    }

    @Test
    @DisplayName("play should wrap runtime exceptions in DomainException")
    void play_shouldWrapRuntimeExceptions() {
        when(randomHandProvider.getRandomHand())
                .thenThrow(new RuntimeException("Unexpected error"));

        assertThatThrownBy(() -> gameService.play(Hand.ROCK))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Failed to play game")
                .hasMessageContaining("Unexpected error")
                .extracting("errorCode").isEqualTo("GAME_ERROR");
    }

    @Test
    @DisplayName("play should call RandomHandProvider exactly once per game")
    void play_shouldCallRandomHandProviderOnce() {
        when(randomHandProvider.getRandomHand()).thenReturn(Hand.ROCK);

        gameService.play(Hand.PAPER);

        verify(randomHandProvider, times(1)).getRandomHand();
        verifyNoMoreInteractions(randomHandProvider);
    }

    @Test
    @DisplayName("play should generate unique game IDs for consecutive games")
    void play_shouldGenerateUniqueGameIds() {
        when(randomHandProvider.getRandomHand()).thenReturn(Hand.ROCK);

        Game game1 = gameService.play(Hand.PAPER);
        Game game2 = gameService.play(Hand.PAPER);

        assertThat(game1.getGameId()).isNotEqualTo(game2.getGameId());
        verify(randomHandProvider, times(2)).getRandomHand();
    }

    @Test
    @DisplayName("play should handle all computer hand options")
    void play_shouldHandleAllComputerHandOptions() {
        when(randomHandProvider.getRandomHand())
                .thenReturn(Hand.ROCK)
                .thenReturn(Hand.PAPER)
                .thenReturn(Hand.SCISSORS);

        Game game1 = gameService.play(Hand.ROCK);
        Game game2 = gameService.play(Hand.ROCK);
        Game game3 = gameService.play(Hand.ROCK);

        assertAll(
                () -> assertThat(game1.getComputerHand()).isEqualTo(Hand.ROCK),
                () -> assertThat(game2.getComputerHand()).isEqualTo(Hand.PAPER),
                () -> assertThat(game3.getComputerHand()).isEqualTo(Hand.SCISSORS)
        );
    }
}
