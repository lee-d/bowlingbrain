package de.europace.bowlingbrain.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    @Nested
    class CreateGame {

        @Test
        void shouldCreateGameWithPlayers() {
            when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArgument(0));

            Game result = gameService.createGame(List.of("Alice", "Bob"));

            assertThat(result.getPlayers()).hasSize(2);
            assertThat(result.getPlayers().get(0).getName()).isEqualTo("Alice");
            assertThat(result.getPlayers().get(1).getName()).isEqualTo("Bob");
        }

        @Test
        void shouldSetCurrentPlayerToFirstPlayer() {
            when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArgument(0));

            Game result = gameService.createGame(List.of("Alice", "Bob"));

            assertThat(result.getCurrentPlayerId()).isEqualTo(result.getPlayers().get(0).getId());
        }

        @Test
        void shouldCreateGameWithSinglePlayer() {
            when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArgument(0));

            Game result = gameService.createGame(List.of("Alice"));

            assertThat(result.getPlayers()).hasSize(1);
            assertThat(result.getCurrentPlayerId()).isEqualTo(result.getPlayers().get(0).getId());
        }

        @Test
        void shouldSaveGameToRepository() {
            when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArgument(0));

            gameService.createGame(List.of("Alice"));

            verify(gameRepository).save(any(Game.class));
        }
    }

    @Nested
    class GetGame {

        @Test
        void shouldReturnGameWhenFound() {
            Game game = createGameWithPlayer("Alice");
            when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

            Game result = gameService.getGame("game-1");

            assertThat(result).isEqualTo(game);
        }

        @Test
        void shouldCalculateScoresWhenGettingGame() {
            Game game = createGameWithPlayer("Alice");
            Player player = game.getPlayers().get(0);
            Frame frame = new Frame();
            frame.getRolls().add(new Roll(3));
            frame.getRolls().add(new Roll(4));
            player.getFrames().add(frame);
            when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

            Game result = gameService.getGame("game-1");

            assertThat(result.getPlayers().get(0).getFrames().get(0).getScore()).isEqualTo(7);
        }

        @Test
        void shouldThrowNotFoundWhenGameDoesNotExist() {
            when(gameRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> gameService.getGame("unknown"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Game not found");
        }
    }

    @Nested
    class AddPlayer {

        @Test
        void shouldAddPlayerToExistingGame() {
            Game game = createGameWithPlayer("Alice");
            when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));
            when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArgument(0));

            Game result = gameService.addPlayer("game-1", "Bob");

            assertThat(result.getPlayers()).hasSize(2);
            assertThat(result.getPlayers().get(1).getName()).isEqualTo("Bob");
        }

        @Test
        void shouldThrowNotFoundWhenGameDoesNotExist() {
            when(gameRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> gameService.addPlayer("unknown", "Bob"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Game not found");
        }
    }

    @Nested
    class AddFrame {

        @Test
        void shouldAddFrameToPlayer() {
            Game game = createGameWithPlayer("Alice");
            String playerId = game.getPlayers().get(0).getId();
            when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));
            when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArgument(0));

            Game result = gameService.addFrame("game-1", playerId);

            assertThat(result.getPlayers().get(0).getFrames()).hasSize(1);
        }

        @Test
        void shouldThrowNotFoundWhenPlayerDoesNotExist() {
            Game game = createGameWithPlayer("Alice");
            when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

            assertThatThrownBy(() -> gameService.addFrame("game-1", "unknown-player"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Player not found");
        }
    }

    @Nested
    class AddRoll {

        @Test
        void shouldAddRollToFrame() {
            Game game = createGameWithPlayerAndFrame("Alice");
            String playerId = game.getPlayers().get(0).getId();
            String frameId = game.getPlayers().get(0).getFrames().get(0).getId();
            when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));
            when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArgument(0));

            Game result = gameService.addRoll("game-1", playerId, frameId, 7);

            assertThat(result.getPlayers().get(0).getFrames().get(0).getRolls()).hasSize(1);
            assertThat(result.getPlayers().get(0).getFrames().get(0).getRolls().get(0).getSmashedPins()).isEqualTo(7);
        }

        @Test
        void shouldCalculateScoresAfterAddingRoll() {
            Game game = createGameWithPlayerAndFrame("Alice");
            String playerId = game.getPlayers().get(0).getId();
            String frameId = game.getPlayers().get(0).getFrames().get(0).getId();
            when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));
            when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArgument(0));

            gameService.addRoll("game-1", playerId, frameId, 5);

            assertThat(game.getPlayers().get(0).getFrames().get(0).getScore()).isEqualTo(5);
        }

        @Test
        void shouldThrowNotFoundWhenPlayerDoesNotExist() {
            Game game = createGameWithPlayerAndFrame("Alice");
            when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

            assertThatThrownBy(() -> gameService.addRoll("game-1", "unknown", "frame-id", 5))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Player not found");
        }

        @Test
        void shouldThrowNotFoundWhenFrameDoesNotExist() {
            Game game = createGameWithPlayerAndFrame("Alice");
            String playerId = game.getPlayers().get(0).getId();
            when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

            assertThatThrownBy(() -> gameService.addRoll("game-1", playerId, "unknown-frame", 5))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Frame not found");
        }
    }

    @Nested
    class AdvanceToNextPlayer {

        @Test
        void shouldAdvanceToSecondPlayer() {
            Game game = createGameWithTwoPlayers();
            when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));
            when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArgument(0));

            Game result = gameService.advanceToNextPlayer("game-1");

            assertThat(result.getCurrentPlayerId()).isEqualTo(game.getPlayers().get(1).getId());
        }

        @Test
        void shouldWrapAroundToFirstPlayer() {
            Game game = createGameWithTwoPlayers();
            game.setCurrentPlayerId(game.getPlayers().get(1).getId());
            when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));
            when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArgument(0));

            Game result = gameService.advanceToNextPlayer("game-1");

            assertThat(result.getCurrentPlayerId()).isEqualTo(game.getPlayers().get(0).getId());
        }

        @Test
        void shouldStayOnSamePlayerWhenOnlyOnePlayer() {
            Game game = createGameWithPlayer("Alice");
            when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));
            when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArgument(0));

            Game result = gameService.advanceToNextPlayer("game-1");

            assertThat(result.getCurrentPlayerId()).isEqualTo(game.getPlayers().get(0).getId());
        }

        @Test
        void shouldThrowNotFoundWhenCurrentPlayerIdIsInvalid() {
            Game game = createGameWithPlayer("Alice");
            game.setCurrentPlayerId("invalid-id");
            when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));

            assertThatThrownBy(() -> gameService.advanceToNextPlayer("game-1"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Current player not found");
        }

        @Test
        void shouldThrowNotFoundWhenGameDoesNotExist() {
            when(gameRepository.findById("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> gameService.advanceToNextPlayer("unknown"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Game not found");
        }
    }

    private Game createGameWithPlayer(String name) {
        Game game = new Game();
        game.setId("game-1");
        Player player = new Player(name);
        game.setPlayers(new ArrayList<>(List.of(player)));
        game.setCurrentPlayerId(player.getId());
        return game;
    }

    private Game createGameWithTwoPlayers() {
        Game game = new Game();
        game.setId("game-1");
        Player alice = new Player("Alice");
        Player bob = new Player("Bob");
        game.setPlayers(new ArrayList<>(List.of(alice, bob)));
        game.setCurrentPlayerId(alice.getId());
        return game;
    }

    private Game createGameWithPlayerAndFrame(String name) {
        Game game = createGameWithPlayer(name);
        game.getPlayers().get(0).getFrames().add(new Frame());
        return game;
    }
}

