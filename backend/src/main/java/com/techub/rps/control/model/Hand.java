package com.techub.rps.control.model;

import java.util.Arrays;
import java.util.List;

public enum Hand {
    ROCK,
    PAPER,
    SCISSORS;

    public static List<Hand> getAllHands() {
        return Arrays.asList(values());
    }

    public GameResult playAgainst(Hand opponent) {
        if (this == opponent) {
            return GameResult.DRAW;
        }

        return switch (this) {
            case ROCK -> opponent == SCISSORS ? GameResult.WIN : GameResult.LOSE;
            case PAPER -> opponent == ROCK ? GameResult.WIN : GameResult.LOSE;
            case SCISSORS -> opponent == PAPER ? GameResult.WIN : GameResult.LOSE;
        };
    }
}
