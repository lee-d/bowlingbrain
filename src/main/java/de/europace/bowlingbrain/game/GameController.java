package de.europace.bowlingbrain.game;

import de.europace.bowlingbrain.api.GamesApi;
import de.europace.bowlingbrain.api.dto.AddPlayerRequest;
import de.europace.bowlingbrain.api.dto.AddRollRequest;
import de.europace.bowlingbrain.api.dto.CreateGameRequest;
import de.europace.bowlingbrain.api.dto.GameDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class GameController implements GamesApi {

    private final GameService gameService;
    private final GameMapper gameMapper;
    private final GameLinkEnricher gameLinkEnricher;

    @Override
    public ResponseEntity<GameDto> createGame(CreateGameRequest createGameRequest) {
        log.info("POST /games - Creating game with players: {}", createGameRequest.getPlayerNames());
        long start = System.nanoTime();
        final Game game = gameService.createGame(createGameRequest.getPlayerNames());
        log.info("POST /games - 201 CREATED - gameId={} - {}ms", game.getId(), elapsedMs(start));
        return ResponseEntity.status(HttpStatus.CREATED).body(enrich(game));
    }

    @Override
    public ResponseEntity<GameDto> getGame(String gameId) {
        log.info("GET /games/{} - Retrieving game", gameId);
        long start = System.nanoTime();
        final Game game = gameService.getGame(gameId);
        log.info("GET /games/{} - 200 OK - {}ms", gameId, elapsedMs(start));
        return ResponseEntity.ok(enrich(game));
    }

    @Override
    public ResponseEntity<GameDto> addPlayer(String gameId, AddPlayerRequest addPlayerRequest) {
        log.info("POST /games/{}/players - Adding player: {}", gameId, addPlayerRequest.getPlayerName());
        long start = System.nanoTime();
        final Game game = gameService.addPlayer(gameId, addPlayerRequest.getPlayerName());
        log.info("POST /games/{}/players - 201 CREATED - {}ms", gameId, elapsedMs(start));
        return ResponseEntity.status(HttpStatus.CREATED).body(enrich(game));
    }

    @Override
    public ResponseEntity<GameDto> addFrame(String gameId, String playerId) {
        log.info("POST /games/{}/players/{}/frames - Adding frame", gameId, playerId);
        long start = System.nanoTime();
        final Game game = gameService.addFrame(gameId, playerId);
        log.info("POST /games/{}/players/{}/frames - 201 CREATED - {}ms", gameId, playerId, elapsedMs(start));
        return ResponseEntity.status(HttpStatus.CREATED).body(enrich(game));
    }

    @Override
    public ResponseEntity<GameDto> addRoll(String gameId, String playerId, String frameId, AddRollRequest addRollRequest) {
        log.info("POST /games/{}/players/{}/frames/{}/rolls - smashedPins={}", gameId, playerId, frameId, addRollRequest.getSmashedPins());
        long start = System.nanoTime();
        final Game game = gameService.addRoll(gameId, playerId, frameId, addRollRequest.getSmashedPins());
        log.info("POST /games/{}/players/{}/frames/{}/rolls - 201 CREATED - {}ms", gameId, playerId, frameId, elapsedMs(start));
        return ResponseEntity.status(HttpStatus.CREATED).body(enrich(game));
    }

    @Override
    public ResponseEntity<GameDto> advanceToNextPlayer(String gameId) {
        log.info("POST /games/{}/current-player/next - Advancing to next player", gameId);
        long start = System.nanoTime();
        final Game game = gameService.advanceToNextPlayer(gameId);
        log.info("POST /games/{}/current-player/next - 200 OK - {}ms", gameId, elapsedMs(start));
        return ResponseEntity.ok(enrich(game));
    }

    private GameDto enrich(Game game) {
        return gameLinkEnricher.enrich(gameMapper.toDto(game));
    }

    private long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
