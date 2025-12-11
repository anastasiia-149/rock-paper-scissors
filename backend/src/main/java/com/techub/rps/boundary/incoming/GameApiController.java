package com.techub.rps.boundary.incoming;

import com.techub.rps.boundary.incoming.api.GameApi;
import com.techub.rps.boundary.incoming.dto.GameResponse;
import com.techub.rps.boundary.incoming.dto.PlayGameRequest;
import com.techub.rps.boundary.incoming.dto.RegisterUserRequest;
import com.techub.rps.boundary.incoming.dto.UserResponse;
import com.techub.rps.boundary.incoming.dto.UserStatisticsResponse;
import com.techub.rps.control.model.Game;
import com.techub.rps.control.model.Hand;
import com.techub.rps.control.model.User;
import com.techub.rps.control.model.UserStatistics;
import com.techub.rps.control.GameService;
import com.techub.rps.control.StatisticsService;
import com.techub.rps.control.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GameApiController implements GameApi {

    private final GameService gameService;
    private final GameMapper gameMapper;
    private final UserRegistrationService userRegistrationService;
    private final StatisticsService statisticsService;

    @Override
    public ResponseEntity<GameResponse> playGame(PlayGameRequest request) {
        log.info("Received play game request: {}", request);

        Hand playerHand = gameMapper.toDomainHand(request.getPlayerHand());
        String username = request.getUsername();
        Game game = gameService.play(username, playerHand);

        GameResponse response = gameMapper.toDto(game);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserResponse> registerUser(RegisterUserRequest request) {
        log.info("Received register user request: {}", request);

        User user = userRegistrationService.registerUser(request.getUsername());
        UserResponse response = gameMapper.toUserDto(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<UserStatisticsResponse> getUserStatistics(String username) {
        log.info("Received get statistics request for user: {}", username);

        UserStatistics stats = statisticsService.getUserStatistics(username);
        UserStatisticsResponse response = gameMapper.toStatisticsDto(stats);

        return ResponseEntity.ok(response);
    }
}
