package com.techub.rps.control.ports;

import com.techub.rps.control.exception.DomainException;
import com.techub.rps.control.model.Game;
import com.techub.rps.control.model.Hand;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final RandomHandProvider randomHandProvider;
    private final MetricsProvider metricsPort;
    private final UserStatisticsPort userStatisticsPort;

    public Game play(String username, Hand playerHand) {
        Timer.Sample timer = metricsPort.startTimer();

        try {
            validateUsername(username);
            validatePlayerHand(playerHand);

            log.info("Playing game with username: {}, player hand: {}", username, playerHand);

            Hand computerHand = randomHandProvider.getRandomHand();
            log.debug("Computer chose: {}", computerHand);

            Game game = Game.play(playerHand, computerHand);

            userStatisticsPort.updateStatistics(username, game);
            metricsPort.recordGamePlayed(game);

            log.info("Game result - ID: {}, Username: {}, Player: {}, Computer: {}, Result: {}",
                    game.getGameId(), username, game.getPlayerHand(), game.getComputerHand(), game.getResult());

            return game;
        } catch (DomainException ex) {
            if (ex.getErrorType() == DomainException.ErrorType.CLIENT_ERROR) {
                String errorMetric = switch (ex.getErrorCode()) {
                    case "INVALID_HAND" -> "invalid_hand";
                    case "INVALID_USERNAME" -> "invalid_username";
                    default -> "client_error";
                };
                metricsPort.recordError(errorMetric);
            } else {
                metricsPort.recordError("game_error");
            }
            throw ex;
        } catch (Exception ex) {
            metricsPort.recordError("game_error");
            log.error("Failed to play game", ex);
            throw DomainException.gameError("Failed to play game: " + ex.getMessage(), ex);
        } finally {
            metricsPort.stopTimer(timer);
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw DomainException.invalidUsername("Username cannot be null or empty");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw DomainException.invalidUsername("Username must be between 3 and 50 characters");
        }
    }

    private void validatePlayerHand(Hand playerHand) {
        if (playerHand == null) {
            throw DomainException.invalidHand("Player hand cannot be null");
        }
    }
}
