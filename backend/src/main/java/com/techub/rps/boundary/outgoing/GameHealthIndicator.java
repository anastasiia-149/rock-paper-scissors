package com.techub.rps.boundary.outgoing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for the Rock Paper Scissors game service.
 * Provides detailed health information about the game.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GameHealthIndicator implements HealthIndicator {

    private final MetricsHandler metricsService;

    @Override
    public Health health() {
        try {
            long totalGames = metricsService.getTotalGames();
            long totalWins = metricsService.getTotalWins();
            long totalLosses = metricsService.getTotalLosses();
            long totalDraws = metricsService.getTotalDraws();
            double winRate = metricsService.getWinRate();

            return Health.up()
                    .withDetail("totalGames", totalGames)
                    .withDetail("wins", totalWins)
                    .withDetail("losses", totalLosses)
                    .withDetail("draws", totalDraws)
                    .withDetail("winRate", String.format("%.2f%%", winRate))
                    .withDetail("status", "Game service operational")
                    .build();

        } catch (Exception ex) {
            log.error("Health check failed", ex);
            return Health.down()
                    .withDetail("error", ex.getMessage())
                    .withDetail("status", "Game service unavailable")
                    .build();
        }
    }
}
