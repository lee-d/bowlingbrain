package de.europace.bowlingbrain.game;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Game createGame(List<String> playerNames) {
        final List<Player> players = playerNames.stream().map(Player::new).toList();
        final Game game = new Game();
        game.setPlayers(new ArrayList<>(players));
        game.setCurrentPlayerId(players.getFirst().getId());
        return gameRepository.save(game);
    }

    public Game getGame(String gameId) {
        final Game game = loadGame(gameId);
        game.getPlayers().forEach(Player::calculateScores);
        return game;
    }

    public Game addPlayer(String gameId, String playerName) {
        final Game game = loadGame(gameId);
        game.getPlayers().add(new Player(playerName));
        return gameRepository.save(game);
    }

    public Game addFrame(String gameId, String playerId) {
        final Game game = loadGame(gameId);
        findPlayer(game, playerId).getFrames().add(new Frame());
        return gameRepository.save(game);
    }

    public Game addRoll(String gameId, String playerId, String frameId, int smashedPins) {
        final Game game = loadGame(gameId);
        final Player player = findPlayer(game, playerId);
        findFrame(player, frameId).getRolls().add(new Roll(smashedPins));
        player.calculateScores();
        return gameRepository.save(game);
    }

    private Game loadGame(String gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    private Player findPlayer(Game game, String playerId) {
        return game.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
    }

    private Frame findFrame(Player player, String frameId) {
        return player.getFrames().stream()
                .filter(f -> f.getId().equals(frameId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Frame not found"));
    }
}
