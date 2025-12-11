package com.techub.rps.boundary.outgoing;

import com.techub.rps.control.model.Hand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("SecureRandomHandAdapter Tests")
class SecureRandomHandAdapterTest {

    private SecureRandomHandAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SecureRandomHandAdapter();
    }

    @Test
    @DisplayName("getRandomHand should return a non-null hand")
    void getRandomHand_shouldReturnNonNull() {
        Hand hand = adapter.getRandomHand();
        assertThat(hand).isNotNull();
    }

    @Test
    @DisplayName("getRandomHand should return a valid hand option")
    void getRandomHand_shouldReturnValidHand() {
        Hand hand = adapter.getRandomHand();
        assertThat(hand).isIn(Hand.ROCK, Hand.PAPER, Hand.SCISSORS);
    }

    @Test
    @DisplayName("getRandomHand should eventually produce all three hand types")
    void getRandomHand_shouldProduceAllHandTypes() {
        // given
        Set<Hand> generatedHands = new HashSet<>();

        // when
        // statistically close to certain to get all 3 types
        for (int i = 0; i < 200; i++) {
            Hand hand = adapter.getRandomHand();
            generatedHands.add(hand);
            if (generatedHands.size() == 3) {
                break;
            }
        }

        // then
        assertAll(
                () -> assertThat(generatedHands).contains(Hand.ROCK),
                () -> assertThat(generatedHands).contains(Hand.PAPER),
                () -> assertThat(generatedHands).contains(Hand.SCISSORS),
                () -> assertThat(generatedHands).hasSize(3)
        );
    }
}
