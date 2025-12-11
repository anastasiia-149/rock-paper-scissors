package com.techub.rps.control.model;

import lombok.Builder;
import lombok.Value;
import java.time.Instant;

@Value
@Builder
public class UserStatistics {
    String username;
    Integer gamesPlayed;
    Integer wins;
    Integer losses;
    Integer draws;
    String lastGameId;
    Instant lastGamePlayedAt;
}
