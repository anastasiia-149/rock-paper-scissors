package com.techub.rps.control.ports;

import com.techub.rps.control.model.UserStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsService Tests")
class StatisticsServiceTest {

    @Mock
    private UserStatisticsPort userStatisticsPort;

    private StatisticsService statisticsService;

    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsService(userStatisticsPort);
    }

    @Test
    @DisplayName("getUserStatistics should return statistics from port")
    void getUserStatistics_shouldReturnStatisticsFromPort() {
        UserStatistics expectedStats = UserStatistics.builder()
                .username(TEST_USERNAME)
                .gamesPlayed(10)
                .wins(5)
                .losses(3)
                .draws(2)
                .lastGameId("game-123")
                .lastGamePlayedAt(Instant.now())
                .build();

        when(userStatisticsPort.getStatistics(TEST_USERNAME)).thenReturn(expectedStats);

        UserStatistics result = statisticsService.getUserStatistics(TEST_USERNAME);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(result.getGamesPlayed()).isEqualTo(10);
        assertThat(result.getWins()).isEqualTo(5);
        assertThat(result.getLosses()).isEqualTo(3);
        assertThat(result.getDraws()).isEqualTo(2);
        assertThat(result.getLastGameId()).isEqualTo("game-123");
        assertThat(result.getLastGamePlayedAt()).isNotNull();

        verify(userStatisticsPort, times(1)).getStatistics(TEST_USERNAME);
    }

    @Test
    @DisplayName("getUserStatistics should handle zero games played")
    void getUserStatistics_shouldHandleZeroGamesPlayed() {
        UserStatistics expectedStats = UserStatistics.builder()
                .username(TEST_USERNAME)
                .gamesPlayed(0)
                .wins(0)
                .losses(0)
                .draws(0)
                .lastGameId(null)
                .lastGamePlayedAt(null)
                .build();

        when(userStatisticsPort.getStatistics(TEST_USERNAME)).thenReturn(expectedStats);

        UserStatistics result = statisticsService.getUserStatistics(TEST_USERNAME);

        assertThat(result).isNotNull();
        assertThat(result.getGamesPlayed()).isEqualTo(0);
        assertThat(result.getWins()).isEqualTo(0);
        assertThat(result.getLosses()).isEqualTo(0);
        assertThat(result.getDraws()).isEqualTo(0);
        assertThat(result.getLastGameId()).isNull();
        assertThat(result.getLastGamePlayedAt()).isNull();

        verify(userStatisticsPort, times(1)).getStatistics(TEST_USERNAME);
    }

    @Test
    @DisplayName("getUserStatistics should pass username to port correctly")
    void getUserStatistics_shouldPassUsernameToPortCorrectly() {
        String customUsername = "customuser123";
        UserStatistics expectedStats = UserStatistics.builder()
                .username(customUsername)
                .gamesPlayed(1)
                .wins(1)
                .losses(0)
                .draws(0)
                .lastGameId("game-789")
                .lastGamePlayedAt(Instant.now())
                .build();

        when(userStatisticsPort.getStatistics(customUsername)).thenReturn(expectedStats);

        UserStatistics result = statisticsService.getUserStatistics(customUsername);

        assertThat(result.getUsername()).isEqualTo(customUsername);
        verify(userStatisticsPort, times(1)).getStatistics(customUsername);
    }
}
