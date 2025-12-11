package com.techub.rps.boundary.incoming;

import com.techub.rps.boundary.incoming.api.GameApi;
import com.techub.rps.boundary.incoming.dto.GameResponse;
import com.techub.rps.boundary.incoming.dto.PlayGameRequest;
import com.techub.rps.control.model.Game;
import com.techub.rps.control.model.Hand;
import com.techub.rps.control.ports.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GameApiController implements GameApi {

    private final GameService gameService;
    private final GameMapper gameMapper;

    @Override
    public ResponseEntity<GameResponse> playGame(PlayGameRequest request) {
        log.info("Received play game request: {}", request);

        Hand playerHand = gameMapper.toDomainHand(request.getPlayerHand());
        Game game = gameService.play(playerHand);

        GameResponse response = gameMapper.toDto(game);
        return ResponseEntity.ok(response);
    }
}
