package com.techub.rps.control.model;

import com.techub.rps.control.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Game Domain Model Tests")
class GameTest {

    private static final String GAME_ID_PATTERN = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

    @Test
    @DisplayName("play should create a valid game with all required fields")
    void play_shouldCreateValidGame() {
        Instant beforeGame = Instant.now();

        Game game = Game.play(Hand.ROCK, Hand.SCISSORS);

        Instant afterGame = Instant.now();

        assertAll(
                () -> assertThat(game.getGameId()).isNotNull().isNotEmpty(),
                () -> assertThat(game.getPlayerHand()).isEqualTo(Hand.ROCK),
                () -> assertThat(game.getComputerHand()).isEqualTo(Hand.SCISSORS),
                () -> assertThat(game.getResult()).isEqualTo(GameResult.WIN),
                () -> assertThat(game.getTimestamp())
                        .isNotNull()
                        .isAfterOrEqualTo(beforeGame)
                        .isBeforeOrEqualTo(afterGame)
        );
    }

    @Test
    @DisplayName("play should generate unique game IDs")
    void play_shouldGenerateUniqueGameIds() {
        Game game1 = Game.play(Hand.ROCK, Hand.SCISSORS);
        Game game2 = Game.play(Hand.ROCK, Hand.SCISSORS);

        assertThat(game1.getGameId()).isNotEqualTo(game2.getGameId());
    }

    @Test
    @DisplayName("play should generate valid UUID format for game ID")
    void play_shouldGenerateValidUUID() {
        Game game = Game.play(Hand.ROCK, Hand.SCISSORS);

        assertThat(game.getGameId()).matches(GAME_ID_PATTERN);
    }

    @ParameterizedTest(name = "{0} vs {1} should result in {2}")
    @MethodSource("provideGameScenarios")
    @DisplayName("play should calculate correct results for all scenarios")
    void play_shouldCalculateCorrectResults(
            Hand playerHand,
            Hand computerHand,
            GameResult expectedResult) {
        Game game = Game.play(playerHand, computerHand);

        assertAll(
                () -> assertThat(game.getPlayerHand()).isEqualTo(playerHand),
                () -> assertThat(game.getComputerHand()).isEqualTo(computerHand),
                () -> assertThat(game.getResult()).isEqualTo(expectedResult)
        );
    }

    private static Stream<Arguments> provideGameScenarios() {
        return Stream.of(
                // Player wins scenarios
                Arguments.of(Hand.ROCK, Hand.SCISSORS, GameResult.WIN),
                Arguments.of(Hand.PAPER, Hand.ROCK, GameResult.WIN),
                Arguments.of(Hand.SCISSORS, Hand.PAPER, GameResult.WIN),

                // Player loses scenarios
                Arguments.of(Hand.ROCK, Hand.PAPER, GameResult.LOSE),
                Arguments.of(Hand.PAPER, Hand.SCISSORS, GameResult.LOSE),
                Arguments.of(Hand.SCISSORS, Hand.ROCK, GameResult.LOSE),

                // Draw scenarios
                Arguments.of(Hand.ROCK, Hand.ROCK, GameResult.DRAW),
                Arguments.of(Hand.PAPER, Hand.PAPER, GameResult.DRAW),
                Arguments.of(Hand.SCISSORS, Hand.SCISSORS, GameResult.DRAW)
        );
    }

    @Test
    @DisplayName("play should throw DomainException when player hand is null")
    void play_shouldThrowException_whenPlayerHandIsNull() {
        assertThatThrownBy(() -> Game.play(null, Hand.ROCK))
                .isInstanceOf(DomainException.class)
                .hasMessage("Player hand cannot be null");
    }

    @Test
    @DisplayName("play should throw DomainException when computer hand is null")
    void play_shouldThrowException_whenComputerHandIsNull() {
        assertThatThrownBy(() -> Game.play(Hand.ROCK, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Computer hand cannot be null");
    }

    @Test
    @DisplayName("play should throw DomainException when both hands are null")
    void play_shouldThrowException_whenBothHandsAreNull() {
        assertThatThrownBy(() -> Game.play(null, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("Player hand cannot be null");
    }
}
