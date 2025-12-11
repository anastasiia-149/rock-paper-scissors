package com.techub.rps.boundary.outgoing;

import com.techub.rps.control.model.Game;
import com.techub.rps.control.model.GameResult;
import com.techub.rps.control.model.Hand;
import com.techub.rps.control.ports.MetricsProvider;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class MetricsHandler implements MetricsProvider {

    private final MeterRegistry meterRegistry;
    private final AtomicLong totalGames;
    private final AtomicLong totalWins;
    private final AtomicLong totalLosses;
    private final AtomicLong totalDraws;

    public MetricsHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.totalGames = meterRegistry.gauge("games.total", new AtomicLong(0));
        this.totalWins = meterRegistry.gauge("games.wins.total", new AtomicLong(0));
        this.totalLosses = meterRegistry.gauge("games.losses.total", new AtomicLong(0));
        this.totalDraws = meterRegistry.gauge("games.draws.total", new AtomicLong(0));
        log.info("GameMetricsService initialized with MeterRegistry");
    }

    @Override
    public void recordGamePlayed(Game game) {
        if (game == null) {
            log.warn("Attempted to record metrics for null game");
            return;
        }

        totalGames.incrementAndGet();
        recordGameResult(game.getResult());
        recordPlayerHandChoice(game.getPlayerHand());
        recordComputerHandChoice(game.getComputerHand());
        recordHandCombination(game.getPlayerHand(), game.getComputerHand());

        log.debug("Recorded metrics for game: {} - Result: {}, Player: {}, Computer: {}",
                game.getGameId(), game.getResult(), game.getPlayerHand(), game.getComputerHand());
    }

    @Override
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    @Override
    public void stopTimer(Timer.Sample sample) {
        if (sample != null) {
            sample.stop(meterRegistry.timer("games.duration"));
        }
    }

    @Override
    public void recordError(String errorType) {
        Counter.builder("games.errors")
                .tag("type", errorType)
                .register(meterRegistry)
                .increment();

        log.debug("Recorded error metric: {}", errorType);
    }

    public double getWinRate() {
        long total = totalGames.get();
        if (total == 0) {
            return 0.0;
        }
        return (totalWins.get() * 100.0) / total;
    }

    private void recordGameResult(GameResult result) {
        switch (result) {
            case WIN -> totalWins.incrementAndGet();
            case LOSE -> totalLosses.incrementAndGet();
            case DRAW -> totalDraws.incrementAndGet();
        }

        Counter.builder("games.played")
                .tag("result", result.name())
                .description("Total games played by result")
                .register(meterRegistry)
                .increment();
    }

    private void recordPlayerHandChoice(Hand hand) {
        Counter.builder("games.player.hand")
                .tag("choice", hand.name())
                .description("Player hand choices")
                .register(meterRegistry)
                .increment();
    }

    private void recordComputerHandChoice(Hand hand) {
        Counter.builder("games.computer.hand")
                .tag("choice", hand.name())
                .description("Computer hand choices")
                .register(meterRegistry)
                .increment();
    }

    private void recordHandCombination(Hand playerHand, Hand computerHand) {
        Counter.builder("games.combinations")
                .tag("player", playerHand.name())
                .tag("computer", computerHand.name())
                .description("Hand combinations in games")
                .register(meterRegistry)
                .increment();
    }

    public long getTotalGames() {
        return totalGames.get();
    }

    public long getTotalWins() {
        return totalWins.get();
    }

    public long getTotalLosses() {
        return totalLosses.get();
    }

    public long getTotalDraws() {
        return totalDraws.get();
    }
}
