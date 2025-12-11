package com.techub.rps.boundary.outgoing;

import com.techub.rps.boundary.outgoing.db.UserEntity;
import com.techub.rps.boundary.outgoing.db.UserStatisticsEntity;
import com.techub.rps.boundary.outgoing.db.UserRepository;
import com.techub.rps.boundary.outgoing.db.UserStatisticsRepository;
import com.techub.rps.control.exception.DomainException;
import com.techub.rps.control.model.Game;
import com.techub.rps.control.model.GameResult;
import com.techub.rps.control.model.UserStatistics;
import com.techub.rps.control.ports.UserStatisticsPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserStatisticsAdapter implements UserStatisticsPort {

    private final UserRepository userRepository;
    private final UserStatisticsRepository statisticsRepository;

    @Override
    @Transactional
    public void updateStatistics(String username, Game game) {
        log.info("Updating statistics for user: {}", username);

        UserEntity user = userRepository.findByUsername(username)
                .orElseGet(() -> createNewUser(username));

        UserStatisticsEntity stats = statisticsRepository.findByUserId(user.getId())
                .orElseGet(() -> createNewStatistics(user.getId()));

        updateStatisticsWithGameResult(stats, game);
        statisticsRepository.save(stats);

        log.info("Statistics updated for user: {}", username);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatistics getStatistics(String username) {
        log.info("Getting statistics for user: {}", username);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> DomainException.userNotFound("User not found: " + username));

        UserStatisticsEntity stats = statisticsRepository.findByUserId(user.getId())
                .orElseThrow(() -> DomainException.userNotFound("Statistics not found for user: " + username));

        return mapToDomain(username, stats);
    }

    private UserEntity createNewUser(String username) {
        log.info("Creating new user: {}", username);
        UserEntity user = UserEntity.builder()
                .username(username)
                .build();
        return userRepository.save(user);
    }

    private UserStatisticsEntity createNewStatistics(Long userId) {
        log.info("Creating new statistics for user ID: {}", userId);
        return UserStatisticsEntity.builder()
                .userId(userId)
                .gamesPlayed(0)
                .wins(0)
                .losses(0)
                .draws(0)
                .build();
    }

    private void updateStatisticsWithGameResult(UserStatisticsEntity stats, Game game) {
        stats.setGamesPlayed(stats.getGamesPlayed() + 1);

        if (game.getResult() == GameResult.WIN) {
            stats.setWins(stats.getWins() + 1);
        } else if (game.getResult() == GameResult.LOSE) {
            stats.setLosses(stats.getLosses() + 1);
        } else if (game.getResult() == GameResult.DRAW) {
            stats.setDraws(stats.getDraws() + 1);
        }

        stats.setLastGameId(game.getGameId());
        stats.setLastGamePlayedAt(game.getTimestamp());
    }

    private UserStatistics mapToDomain(String username, UserStatisticsEntity stats) {
        return UserStatistics.builder()
                .username(username)
                .gamesPlayed(stats.getGamesPlayed())
                .wins(stats.getWins())
                .losses(stats.getLosses())
                .draws(stats.getDraws())
                .lastGameId(stats.getLastGameId())
                .lastGamePlayedAt(stats.getLastGamePlayedAt())
                .build();
    }
}
