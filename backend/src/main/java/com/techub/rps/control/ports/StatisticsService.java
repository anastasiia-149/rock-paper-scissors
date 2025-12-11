package com.techub.rps.control.ports;

import com.techub.rps.control.model.UserStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final UserStatisticsPort userStatisticsPort;

    public UserStatistics getUserStatistics(String username) {
        log.info("Fetching statistics for user: {}", username);
        return userStatisticsPort.getStatistics(username);
    }
}
