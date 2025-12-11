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

    public Game play(Hand playerHand) {
        Timer.Sample timer = metricsPort.startTimer();

        try {
            validatePlayerHand(playerHand);

            log.info("Playing game with player hand: {}", playerHand);

            Hand computerHand = randomHandProvider.getRandomHand();
            log.debug("Computer chose: {}", computerHand);

            Game game = Game.play(playerHand, computerHand);

            metricsPort.recordGamePlayed(game);

            log.info("Game result - ID: {}, Player: {}, Computer: {}, Result: {}",
                    game.getGameId(), game.getPlayerHand(), game.getComputerHand(), game.getResult());

            return game;
        } catch (DomainException ex) {
            if (ex.getErrorType() == DomainException.ErrorType.CLIENT_ERROR) {
                metricsPort.recordError("invalid_hand");
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

    private void validatePlayerHand(Hand playerHand) {
        if (playerHand == null) {
            throw DomainException.invalidHand("Player hand cannot be null");
        }
    }
}
