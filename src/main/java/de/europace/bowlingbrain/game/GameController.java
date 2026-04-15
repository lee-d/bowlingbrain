package de.europace.bowlingbrain.game;

import de.europace.bowlingbrain.api.GamesApi;
import de.europace.bowlingbrain.api.dto.AddPlayerRequest;
import de.europace.bowlingbrain.api.dto.AddRollRequest;
import de.europace.bowlingbrain.api.dto.CreateGameRequest;
import de.europace.bowlingbrain.api.dto.GameDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class GameController implements GamesApi {

    private final GameService gameService;
    private final GameMapper gameMapper;

    @Override
    public ResponseEntity<GameDto> createGame(CreateGameRequest createGameRequest) {
        final Game game = gameService.createGame(createGameRequest.getPlayerNames());
        return ResponseEntity.status(HttpStatus.CREATED).body(gameMapper.toDto(game));
    }

    @Override
    public ResponseEntity<GameDto> getGame(String gameId) {
        final Game game = gameService.getGame(gameId);
        return ResponseEntity.ok(gameMapper.toDto(game));
    }

    @Override
    public ResponseEntity<GameDto> addPlayer(String gameId, AddPlayerRequest addPlayerRequest) {
        final Game game = gameService.addPlayer(gameId, addPlayerRequest.getPlayerName());
        return ResponseEntity.status(HttpStatus.CREATED).body(gameMapper.toDto(game));
    }

    @Override
    public ResponseEntity<GameDto> addFrame(String gameId, String playerId) {
        final Game game = gameService.addFrame(gameId, playerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameMapper.toDto(game));
    }

    @Override
    public ResponseEntity<GameDto> addRoll(String gameId, String playerId, String frameId, AddRollRequest addRollRequest) {
        final Game game = gameService.addRoll(gameId, playerId, frameId, addRollRequest.getSmashedPins());
        return ResponseEntity.status(HttpStatus.CREATED).body(gameMapper.toDto(game));
    }
}
