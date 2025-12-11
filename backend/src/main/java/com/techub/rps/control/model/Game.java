package com.techub.rps.control.model;

import com.techub.rps.control.exception.DomainException;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class Game {
    String gameId;
    Hand playerHand;
    Hand computerHand;
    GameResult result;
    Instant timestamp;

    public static Game play(Hand playerHand, Hand computerHand) {
        if (playerHand == null) {
            throw DomainException.invalidHand("Player hand cannot be null");
        }
        if (computerHand == null) {
            throw DomainException.invalidHand("Computer hand cannot be null");
        }

        GameResult result = playerHand.playAgainst(computerHand);

        return Game.builder()
                .gameId(UUID.randomUUID().toString())
                .playerHand(playerHand)
                .computerHand(computerHand)
                .result(result)
                .timestamp(Instant.now())
                .build();
    }
}
