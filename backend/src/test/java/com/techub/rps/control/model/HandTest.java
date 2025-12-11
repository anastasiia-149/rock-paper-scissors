package com.techub.rps.control.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Hand Domain Model Tests")
class HandTest {

    @Test
    @DisplayName("getAllHands should return all three hand options")
    void getAllHands_shouldReturnAllThreeOptions() {
        List<Hand> allHands = Hand.getAllHands();

        assertThat(allHands)
                .hasSize(3)
                .containsExactlyInAnyOrder(Hand.ROCK, Hand.PAPER, Hand.SCISSORS);
    }

    @ParameterizedTest(name = "{0} vs {1} should result in {2}")
    @MethodSource("provideHandCombinations")
    @DisplayName("playAgainst should return correct results for all hand combinations")
    void playAgainst_shouldReturnCorrectResultForAllCombinations(
            Hand playerHand,
            Hand opponentHand,
            GameResult expectedResult) {
        GameResult result = playerHand.playAgainst(opponentHand);
        assertThat(result).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> provideHandCombinations() {
        return Stream.of(
                // ROCK combinations
                Arguments.of(Hand.ROCK, Hand.ROCK, GameResult.DRAW),
                Arguments.of(Hand.ROCK, Hand.PAPER, GameResult.LOSE),
                Arguments.of(Hand.ROCK, Hand.SCISSORS, GameResult.WIN),

                // PAPER combinations
                Arguments.of(Hand.PAPER, Hand.ROCK, GameResult.WIN),
                Arguments.of(Hand.PAPER, Hand.PAPER, GameResult.DRAW),
                Arguments.of(Hand.PAPER, Hand.SCISSORS, GameResult.LOSE),

                // SCISSORS combinations
                Arguments.of(Hand.SCISSORS, Hand.ROCK, GameResult.LOSE),
                Arguments.of(Hand.SCISSORS, Hand.PAPER, GameResult.WIN),
                Arguments.of(Hand.SCISSORS, Hand.SCISSORS, GameResult.DRAW)
        );
    }

    @Test
    @DisplayName("ROCK vs SCISSORS should WIN")
    void rock_shouldBeatScissors() {
        assertThat(Hand.ROCK.playAgainst(Hand.SCISSORS)).isEqualTo(GameResult.WIN);
    }

    @Test
    @DisplayName("ROCK vs PAPER should LOSE")
    void rock_shouldLoseToPaper() {
        assertThat(Hand.ROCK.playAgainst(Hand.PAPER)).isEqualTo(GameResult.LOSE);
    }

    @Test
    @DisplayName("PAPER vs ROCK should WIN")
    void paper_shouldBeatRock() {
        assertThat(Hand.PAPER.playAgainst(Hand.ROCK)).isEqualTo(GameResult.WIN);
    }

    @Test
    @DisplayName("PAPER vs SCISSORS should LOSE")
    void paper_shouldLoseToScissors() {
        assertThat(Hand.PAPER.playAgainst(Hand.SCISSORS)).isEqualTo(GameResult.LOSE);
    }

    @Test
    @DisplayName("SCISSORS vs PAPER should WIN")
    void scissors_shouldBeatPaper() {
        assertThat(Hand.SCISSORS.playAgainst(Hand.PAPER)).isEqualTo(GameResult.WIN);
    }

    @Test
    @DisplayName("SCISSORS vs ROCK should LOSE")
    void scissors_shouldLoseToRock() {
        assertThat(Hand.SCISSORS.playAgainst(Hand.ROCK)).isEqualTo(GameResult.LOSE);
    }

    @Test
    @DisplayName("Same hands should always DRAW")
    void sameHands_shouldAlwaysDraw() {
        assertAll(
                () -> assertThat(Hand.ROCK.playAgainst(Hand.ROCK)).isEqualTo(GameResult.DRAW),
                () -> assertThat(Hand.PAPER.playAgainst(Hand.PAPER)).isEqualTo(GameResult.DRAW),
                () -> assertThat(Hand.SCISSORS.playAgainst(Hand.SCISSORS)).isEqualTo(GameResult.DRAW)
        );
    }
}
