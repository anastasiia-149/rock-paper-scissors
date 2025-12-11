package com.techub.rps.control.ports;

import com.techub.rps.control.model.Game;
import com.techub.rps.control.model.UserStatistics;

public interface UserStatisticsPort {
    void updateStatistics(String username, Game game);
    UserStatistics getStatistics(String username);
}
