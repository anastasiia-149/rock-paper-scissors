package com.techub.rps.boundary.outgoing;

import com.techub.rps.boundary.outgoing.db.UserEntity;
import com.techub.rps.boundary.outgoing.db.UserRepository;
import com.techub.rps.boundary.outgoing.db.UserStatisticsEntity;
import com.techub.rps.boundary.outgoing.db.UserStatisticsRepository;
import com.techub.rps.control.exception.DomainException;
import com.techub.rps.control.model.Game;
import com.techub.rps.control.model.GameResult;
import com.techub.rps.control.model.Hand;
import com.techub.rps.control.model.UserStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserStatisticsAdapter Tests")
class UserStatisticsAdapterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStatisticsRepository statisticsRepository;

    private UserStatisticsAdapter userStatisticsAdapter;

    private static final String TEST_USERNAME = "testuser";
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        userStatisticsAdapter = new UserStatisticsAdapter(userRepository, statisticsRepository);
    }

    @Test
    @DisplayName("updateStatistics should create new user and statistics when user does not exist")
    void updateStatistics_shouldCreateNewUserAndStatistics_whenUserDoesNotExist() {
        Game game = createTestGame(GameResult.WIN);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            return UserEntity.builder()
                    .username(entity.getUsername())
                    .createdAt(Instant.now())
                    .build();
        });
        when(statisticsRepository.findByUserId(any())).thenReturn(Optional.empty());

        userStatisticsAdapter.updateStatistics(TEST_USERNAME, game);

        verify(userRepository, times(1)).findByUsername(TEST_USERNAME);
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(statisticsRepository, times(1)).save(any(UserStatisticsEntity.class));
    }

    @Test
    @DisplayName("updateStatistics should update existing statistics when user exists")
    void updateStatistics_shouldUpdateExistingStatistics_whenUserExists() {
        Game game = createTestGame(GameResult.WIN);
        UserEntity existingUser = createTestUserEntity();
        UserStatisticsEntity existingStats = createTestStatisticsEntity(0, 0, 0, 0);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(existingUser));
        when(statisticsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(existingStats));

        userStatisticsAdapter.updateStatistics(TEST_USERNAME, game);

        verify(userRepository, times(1)).findByUsername(TEST_USERNAME);
        verify(userRepository, never()).save(any());
        verify(statisticsRepository, times(1)).save(any(UserStatisticsEntity.class));
    }

    @Test
    @DisplayName("updateStatistics should increment wins when game result is WIN")
    void updateStatistics_shouldIncrementWins_whenGameResultIsWin() {
        Game game = createTestGame(GameResult.WIN);
        UserEntity existingUser = createTestUserEntity();
        UserStatisticsEntity existingStats = createTestStatisticsEntity(5, 2, 2, 1);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(existingUser));
        when(statisticsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(existingStats));

        userStatisticsAdapter.updateStatistics(TEST_USERNAME, game);

        ArgumentCaptor<UserStatisticsEntity> captor = ArgumentCaptor.forClass(UserStatisticsEntity.class);
        verify(statisticsRepository).save(captor.capture());

        UserStatisticsEntity savedStats = captor.getValue();
        assertThat(savedStats.getGamesPlayed()).isEqualTo(6);
        assertThat(savedStats.getWins()).isEqualTo(3);
        assertThat(savedStats.getLosses()).isEqualTo(2);
        assertThat(savedStats.getDraws()).isEqualTo(1);
    }

    @Test
    @DisplayName("updateStatistics should increment losses when game result is LOSE")
    void updateStatistics_shouldIncrementLosses_whenGameResultIsLose() {
        Game game = createTestGame(GameResult.LOSE);
        UserEntity existingUser = createTestUserEntity();
        UserStatisticsEntity existingStats = createTestStatisticsEntity(5, 2, 2, 1);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(existingUser));
        when(statisticsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(existingStats));

        userStatisticsAdapter.updateStatistics(TEST_USERNAME, game);

        ArgumentCaptor<UserStatisticsEntity> captor = ArgumentCaptor.forClass(UserStatisticsEntity.class);
        verify(statisticsRepository).save(captor.capture());

        UserStatisticsEntity savedStats = captor.getValue();
        assertThat(savedStats.getGamesPlayed()).isEqualTo(6);
        assertThat(savedStats.getWins()).isEqualTo(2);
        assertThat(savedStats.getLosses()).isEqualTo(3);
        assertThat(savedStats.getDraws()).isEqualTo(1);
    }

    @Test
    @DisplayName("updateStatistics should increment draws when game result is DRAW")
    void updateStatistics_shouldIncrementDraws_whenGameResultIsDraw() {
        Game game = createTestGame(GameResult.DRAW);
        UserEntity existingUser = createTestUserEntity();
        UserStatisticsEntity existingStats = createTestStatisticsEntity(5, 2, 2, 1);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(existingUser));
        when(statisticsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(existingStats));

        userStatisticsAdapter.updateStatistics(TEST_USERNAME, game);

        ArgumentCaptor<UserStatisticsEntity> captor = ArgumentCaptor.forClass(UserStatisticsEntity.class);
        verify(statisticsRepository).save(captor.capture());

        UserStatisticsEntity savedStats = captor.getValue();
        assertThat(savedStats.getGamesPlayed()).isEqualTo(6);
        assertThat(savedStats.getWins()).isEqualTo(2);
        assertThat(savedStats.getLosses()).isEqualTo(2);
        assertThat(savedStats.getDraws()).isEqualTo(2);
    }

    @Test
    @DisplayName("updateStatistics should update last game info")
    void updateStatistics_shouldUpdateLastGameInfo() {
        Game game = createTestGame(GameResult.WIN);
        UserEntity existingUser = createTestUserEntity();
        UserStatisticsEntity existingStats = createTestStatisticsEntity(5, 2, 2, 1);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(existingUser));
        when(statisticsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(existingStats));

        userStatisticsAdapter.updateStatistics(TEST_USERNAME, game);

        ArgumentCaptor<UserStatisticsEntity> captor = ArgumentCaptor.forClass(UserStatisticsEntity.class);
        verify(statisticsRepository).save(captor.capture());

        UserStatisticsEntity savedStats = captor.getValue();
        assertThat(savedStats.getLastGameId()).isEqualTo(game.getGameId());
        assertThat(savedStats.getLastGamePlayedAt()).isEqualTo(game.getTimestamp());
    }

    @Test
    @DisplayName("getStatistics should throw DomainException when user not found")
    void getStatistics_shouldThrowDomainException_whenUserNotFound() {
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userStatisticsAdapter.getStatistics(TEST_USERNAME))
                .isInstanceOf(DomainException.class)
                .hasMessage("User not found: " + TEST_USERNAME)
                .extracting("errorCode").isEqualTo("USER_NOT_FOUND");

        verify(userRepository, times(1)).findByUsername(TEST_USERNAME);
        verify(statisticsRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("getStatistics should throw DomainException when statistics not found")
    void getStatistics_shouldThrowDomainException_whenStatisticsNotFound() {
        UserEntity existingUser = createTestUserEntity();

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(existingUser));
        when(statisticsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userStatisticsAdapter.getStatistics(TEST_USERNAME))
                .isInstanceOf(DomainException.class)
                .hasMessage("Statistics not found for user: " + TEST_USERNAME)
                .extracting("errorCode").isEqualTo("USER_NOT_FOUND");

        verify(userRepository, times(1)).findByUsername(TEST_USERNAME);
        verify(statisticsRepository, times(1)).findByUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("getStatistics should return user statistics when user exists")
    void getStatistics_shouldReturnUserStatistics_whenUserExists() {
        UserEntity existingUser = createTestUserEntity();
        UserStatisticsEntity existingStats = createTestStatisticsEntity(10, 5, 3, 2);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(existingUser));
        when(statisticsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(existingStats));

        UserStatistics result = userStatisticsAdapter.getStatistics(TEST_USERNAME);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(result.getGamesPlayed()).isEqualTo(10);
        assertThat(result.getWins()).isEqualTo(5);
        assertThat(result.getLosses()).isEqualTo(3);
        assertThat(result.getDraws()).isEqualTo(2);

        verify(userRepository, times(1)).findByUsername(TEST_USERNAME);
        verify(statisticsRepository, times(1)).findByUserId(TEST_USER_ID);
    }

    private Game createTestGame(GameResult result) {
        return Game.builder()
                .gameId("test-game-id")
                .playerHand(Hand.ROCK)
                .computerHand(Hand.SCISSORS)
                .result(result)
                .timestamp(Instant.now())
                .build();
    }

    private UserEntity createTestUserEntity() {
        return UserEntity.builder()
                .id(TEST_USER_ID)
                .username(TEST_USERNAME)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private UserStatisticsEntity createTestStatisticsEntity(int gamesPlayed, int wins, int losses, int draws) {
        return UserStatisticsEntity.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .gamesPlayed(gamesPlayed)
                .wins(wins)
                .losses(losses)
                .draws(draws)
                .lastGameId("previous-game-id")
                .lastGamePlayedAt(Instant.now().minusSeconds(3600))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("initializeStatistics should create statistics for new user")
    void initializeStatistics_shouldCreateStatistics_forNewUser() {
        UserEntity userEntity = createTestUserEntity();

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(userEntity));
        when(statisticsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());

        userStatisticsAdapter.initializeStatistics(TEST_USERNAME);

        ArgumentCaptor<UserStatisticsEntity> captor = ArgumentCaptor.forClass(UserStatisticsEntity.class);
        verify(statisticsRepository).save(captor.capture());

        UserStatisticsEntity savedStats = captor.getValue();
        assertThat(savedStats.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(savedStats.getGamesPlayed()).isZero();
        assertThat(savedStats.getWins()).isZero();
        assertThat(savedStats.getLosses()).isZero();
        assertThat(savedStats.getDraws()).isZero();
    }

    @Test
    @DisplayName("initializeStatistics should not create statistics if they already exist")
    void initializeStatistics_shouldNotCreateStatistics_ifTheyAlreadyExist() {
        UserEntity userEntity = createTestUserEntity();
        UserStatisticsEntity existingStats = createTestStatisticsEntity(10, 5, 3, 2);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(userEntity));
        when(statisticsRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(existingStats));

        userStatisticsAdapter.initializeStatistics(TEST_USERNAME);

        verify(statisticsRepository, never()).save(any());
    }

    @Test
    @DisplayName("initializeStatistics should throw exception when user does not exist")
    void initializeStatistics_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userStatisticsAdapter.initializeStatistics(TEST_USERNAME))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("User not found");

        verify(statisticsRepository, never()).save(any());
    }
}
