package com.techub.rps.boundary.incoming;

import com.techub.rps.boundary.incoming.dto.GameResponse;
import com.techub.rps.boundary.incoming.dto.UserResponse;
import com.techub.rps.boundary.incoming.dto.UserStatisticsResponse;
import com.techub.rps.control.model.Game;
import com.techub.rps.control.model.User;
import com.techub.rps.control.model.UserStatistics;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class GameMapper {

    public GameResponse toDto(Game game) {
        GameResponse response = new GameResponse();
        response.setGameId(UUID.fromString(game.getGameId()));
        response.setPlayerHand(mapToDtoHand(game.getPlayerHand()));
        response.setComputerHand(mapToDtoHand(game.getComputerHand()));
        response.setResult(mapToDtoResult(game.getResult()));
        response.setTimestamp(convertToUtcOffsetDateTime(game.getTimestamp()));
        return response;
    }

    public UserResponse toUserDto(User user) {
        UserResponse response = new UserResponse();
        response.setUsername(user.getUsername());
        response.setCreatedAt(convertToUtcOffsetDateTime(user.getCreatedAt()));
        return response;
    }

    public UserStatisticsResponse toStatisticsDto(UserStatistics stats) {
        UserStatisticsResponse response = new UserStatisticsResponse();
        response.setUsername(stats.getUsername());
        response.setGamesPlayed(stats.getGamesPlayed());
        response.setWins(stats.getWins());
        response.setLosses(stats.getLosses());
        response.setDraws(stats.getDraws());
        response.setLastGameId(stats.getLastGameId() != null
                ? UUID.fromString(stats.getLastGameId())
                : null);
        response.setLastGamePlayedAt(stats.getLastGamePlayedAt() != null
                ? convertToUtcOffsetDateTime(stats.getLastGamePlayedAt())
                : null);
        return response;
    }

    public com.techub.rps.control.model.Hand toDomainHand(
            com.techub.rps.boundary.incoming.dto.Hand dtoHand) {
        return com.techub.rps.control.model.Hand.valueOf(dtoHand.name());
    }

    private com.techub.rps.boundary.incoming.dto.Hand mapToDtoHand(
            com.techub.rps.control.model.Hand domainHand) {
        return com.techub.rps.boundary.incoming.dto.Hand.valueOf(domainHand.name());
    }

    private com.techub.rps.boundary.incoming.dto.GameResult mapToDtoResult(
            com.techub.rps.control.model.GameResult domainResult) {
        return com.techub.rps.boundary.incoming.dto.GameResult.valueOf(domainResult.name());
    }

    private OffsetDateTime convertToUtcOffsetDateTime(java.time.Instant timestamp) {
        return OffsetDateTime.ofInstant(timestamp, ZoneOffset.UTC);
    }
}
