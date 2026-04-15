package de.europace.bowlingbrain.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class GameRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7");

    @Autowired
    private GameRepository gameRepository;

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
    }

    @Test
    void shouldSaveAndLoadGame() {
        Game game = createGameWithOnePlayer("Alice");

        Game saved = gameRepository.save(game);

        assertThat(saved.getId()).isNotNull();
        Game loaded = gameRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getCurrentPlayerId()).isEqualTo(game.getPlayers().getFirst().getId());
        assertThat(loaded.getPlayers()).hasSize(1);
    }

    @Test
    void shouldPersistPlayerWithFramesAndRolls() {
        Game game = new Game();
        Player player = new Player("Bob");
        Frame frame = new Frame();
        frame.setRolls(List.of(new Roll(7), new Roll(2)));
        player.setFrames(List.of(frame));
        game.setPlayers(List.of(player));

        Game loaded = saveAndReload(game);

        Player loadedPlayer = loaded.getPlayers().getFirst();
        assertThat(loadedPlayer.getName()).isEqualTo("Bob");
        assertThat(loadedPlayer.getFrames()).hasSize(1);
        List<Roll> rolls = loadedPlayer.getFrames().getFirst().getRolls();
        assertThat(rolls).hasSize(2);
        assertThat(rolls.get(0).getSmashedPins()).isEqualTo(7);
        assertThat(rolls.get(1).getSmashedPins()).isEqualTo(2);
    }

    @Test
    void shouldPersistStrikeRoll() {
        Game game = new Game();
        Player player = new Player("Charlie");
        Frame frame = new Frame();
        frame.setRolls(List.of(new Roll(10)));
        player.setFrames(List.of(frame));
        game.setPlayers(List.of(player));

        Game loaded = saveAndReload(game);

        Roll roll = loaded.getPlayers().getFirst().getFrames().getFirst().getRolls().getFirst();
        assertThat(roll.getSmashedPins()).isEqualTo(10);
    }

    @Test
    void shouldPersistMultiplePlayers() {
        Game game = new Game();
        Player bob = new Player("Bob");
        game.setPlayers(List.of(new Player("Alice"), bob, new Player("Charlie")));
        game.setCurrentPlayerId(bob.getId());

        Game loaded = saveAndReload(game);

        assertThat(loaded.getPlayers()).hasSize(3);
        assertThat(loaded.getCurrentPlayerId()).isEqualTo(bob.getId());
        assertThat(loaded.getPlayers().stream().map(Player::getName))
                .containsExactly("Alice", "Bob", "Charlie");
    }

    @Test
    void shouldPersistMultipleFrames() {
        Game game = new Game();
        Player player = new Player("Alice");
        Frame first = new Frame();
        first.setRolls(List.of(new Roll(3), new Roll(4)));
        Frame second = new Frame();
        second.setRolls(List.of(new Roll(10)));
        player.setFrames(List.of(first, second));
        game.setPlayers(List.of(player));

        Game loaded = saveAndReload(game);

        List<Frame> frames = loaded.getPlayers().getFirst().getFrames();
        assertThat(frames).hasSize(2);
        assertThat(frames.get(0).getRolls()).hasSize(2);
        assertThat(frames.get(1).getRolls()).hasSize(1);
    }

    @Test
    void shouldPersistLastFrameWithThreeRolls() {
        Game game = new Game();
        Player player = new Player("Alice");
        Frame lastFrame = new Frame();
        lastFrame.setRolls(List.of(new Roll(10), new Roll(10), new Roll(7)));
        player.setFrames(List.of(lastFrame));
        game.setPlayers(List.of(player));

        Game loaded = saveAndReload(game);

        assertThat(loaded.getPlayers().getFirst().getFrames().getFirst().getRolls()).hasSize(3);
    }

    @Test
    void shouldPersistEmbeddedEntityIds() {
        Game game = new Game();
        Player player = new Player("Alice");
        Frame frame = new Frame();
        Roll roll = new Roll(5);
        frame.setRolls(List.of(roll));
        player.setFrames(List.of(frame));
        game.setPlayers(List.of(player));

        Game loaded = saveAndReload(game);

        Player loadedPlayer = loaded.getPlayers().getFirst();
        Frame loadedFrame = loadedPlayer.getFrames().getFirst();
        Roll loadedRoll = loadedFrame.getRolls().getFirst();
        assertThat(loadedPlayer.getId()).isEqualTo(player.getId());
        assertThat(loadedFrame.getId()).isEqualTo(frame.getId());
        assertThat(loadedRoll.getId()).isEqualTo(roll.getId());
    }

    @Test
    void shouldDeleteGame() {
        Game game = createGameWithOnePlayer("Alice");
        Game saved = gameRepository.save(game);

        gameRepository.deleteById(saved.getId());

        assertThat(gameRepository.findById(saved.getId())).isEmpty();
    }

    private Game createGameWithOnePlayer(String name) {
        Game game = new Game();
        Player player = new Player(name);
        game.setPlayers(List.of(player));
        game.setCurrentPlayerId(player.getId());
        return game;
    }

    private Game saveAndReload(Game game) {
        Game saved = gameRepository.save(game);
        return gameRepository.findById(saved.getId()).orElseThrow();
    }
}
