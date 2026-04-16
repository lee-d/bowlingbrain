package de.europace.bowlingbrain.game;

import de.europace.bowlingbrain.api.dto.FrameDto;
import de.europace.bowlingbrain.api.dto.GameDto;
import de.europace.bowlingbrain.api.dto.PlayerDto;
import de.europace.bowlingbrain.api.dto.RollDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameLinkEnricherTest {

    private GameLinkEnricher enricher;

    @BeforeEach
    void setUp() {
        enricher = new GameLinkEnricher();
    }

    @Nested
    class GameLinks {

        @Test
        void shouldAddSelfLink() {
            GameDto game = gameDto("g1");

            GameDto result = enricher.enrich(game);

            assertThat(result.getLinks()).containsKey("self");
            assertThat(result.getLinks().get("self").getHref()).isEqualTo("/api/v1/games/g1");
        }

        @Test
        void shouldAddNextPlayerLink() {
            GameDto game = gameDto("g1");

            GameDto result = enricher.enrich(game);

            assertThat(result.getLinks()).containsKey("next-player");
            assertThat(result.getLinks().get("next-player").getHref()).isEqualTo("/api/v1/games/g1/current-player/next");
        }

        @Test
        void shouldAddAddPlayerLink() {
            GameDto game = gameDto("g1");

            GameDto result = enricher.enrich(game);

            assertThat(result.getLinks()).containsKey("add-player");
            assertThat(result.getLinks().get("add-player").getHref()).isEqualTo("/api/v1/games/g1/players");
        }

        @Test
        void shouldHandleNullPlayers() {
            GameDto game = new GameDto();
            game.setId("g1");
            game.setPlayers(null);

            GameDto result = enricher.enrich(game);

            assertThat(result.getLinks()).containsKey("self");
        }
    }

    @Nested
    class PlayerLinks {

        @Test
        void shouldAddSelfLinkToPlayer() {
            GameDto game = gameDtoWithPlayer("g1", "p1");

            GameDto result = enricher.enrich(game);

            PlayerDto player = result.getPlayers().get(0);
            assertThat(player.getLinks().get("self").getHref()).isEqualTo("/api/v1/games/g1/players/p1");
        }

        @Test
        void shouldAddAddFrameLinkToPlayer() {
            GameDto game = gameDtoWithPlayer("g1", "p1");

            GameDto result = enricher.enrich(game);

            PlayerDto player = result.getPlayers().get(0);
            assertThat(player.getLinks().get("add-frame").getHref()).isEqualTo("/api/v1/games/g1/players/p1/frames");
        }

        @Test
        void shouldHandleNullFrames() {
            GameDto game = gameDtoWithPlayer("g1", "p1");
            game.getPlayers().get(0).setFrames(null);

            GameDto result = enricher.enrich(game);

            assertThat(result.getPlayers().get(0).getLinks()).containsKey("self");
        }
    }

    @Nested
    class FrameLinks {

        @Test
        void shouldAddSelfLinkToFrame() {
            GameDto game = gameDtoWithFrame("g1", "p1", "f1");

            GameDto result = enricher.enrich(game);

            FrameDto frame = result.getPlayers().get(0).getFrames().get(0);
            assertThat(frame.getLinks().get("self").getHref()).isEqualTo("/api/v1/games/g1/players/p1/frames/f1");
        }

        @Test
        void shouldAddAddRollLinkToFrame() {
            GameDto game = gameDtoWithFrame("g1", "p1", "f1");

            GameDto result = enricher.enrich(game);

            FrameDto frame = result.getPlayers().get(0).getFrames().get(0);
            assertThat(frame.getLinks().get("add-roll").getHref()).isEqualTo("/api/v1/games/g1/players/p1/frames/f1/rolls");
        }

        @Test
        void shouldHandleNullRolls() {
            GameDto game = gameDtoWithFrame("g1", "p1", "f1");
            game.getPlayers().get(0).getFrames().get(0).setRolls(null);

            GameDto result = enricher.enrich(game);

            assertThat(result.getPlayers().get(0).getFrames().get(0).getLinks()).containsKey("self");
        }
    }

    @Nested
    class RollLinks {

        @Test
        void shouldAddSelfLinkToRoll() {
            GameDto game = gameDtoWithRoll("g1", "p1", "f1", "r1");

            GameDto result = enricher.enrich(game);

            RollDto roll = result.getPlayers().get(0).getFrames().get(0).getRolls().get(0);
            assertThat(roll.getLinks().get("self").getHref()).isEqualTo("/api/v1/games/g1/players/p1/frames/f1/rolls/r1");
        }
    }

    @Nested
    class DeepHierarchy {

        @Test
        void shouldEnrichEntireHierarchy() {
            GameDto game = gameDtoWithRoll("g1", "p1", "f1", "r1");

            GameDto result = enricher.enrich(game);

            assertThat(result.getLinks()).isNotEmpty();
            assertThat(result.getPlayers().get(0).getLinks()).isNotEmpty();
            assertThat(result.getPlayers().get(0).getFrames().get(0).getLinks()).isNotEmpty();
            assertThat(result.getPlayers().get(0).getFrames().get(0).getRolls().get(0).getLinks()).isNotEmpty();
        }

        @Test
        void shouldEnrichMultiplePlayers() {
            GameDto game = gameDto("g1");
            PlayerDto p1 = playerDto("p1");
            PlayerDto p2 = playerDto("p2");
            game.setPlayers(new ArrayList<>(List.of(p1, p2)));

            GameDto result = enricher.enrich(game);

            assertThat(result.getPlayers().get(0).getLinks().get("self").getHref()).contains("p1");
            assertThat(result.getPlayers().get(1).getLinks().get("self").getHref()).contains("p2");
        }

        @Test
        void shouldEnrichMultipleFrames() {
            GameDto game = gameDtoWithPlayer("g1", "p1");
            FrameDto f1 = frameDto("f1");
            FrameDto f2 = frameDto("f2");
            game.getPlayers().get(0).setFrames(new ArrayList<>(List.of(f1, f2)));

            GameDto result = enricher.enrich(game);

            assertThat(result.getPlayers().get(0).getFrames().get(0).getLinks().get("self").getHref()).contains("f1");
            assertThat(result.getPlayers().get(0).getFrames().get(1).getLinks().get("self").getHref()).contains("f2");
        }

        @Test
        void shouldEnrichMultipleRolls() {
            GameDto game = gameDtoWithFrame("g1", "p1", "f1");
            RollDto r1 = rollDto("r1");
            RollDto r2 = rollDto("r2");
            game.getPlayers().get(0).getFrames().get(0).setRolls(new ArrayList<>(List.of(r1, r2)));

            GameDto result = enricher.enrich(game);

            List<RollDto> rolls = result.getPlayers().get(0).getFrames().get(0).getRolls();
            assertThat(rolls.get(0).getLinks().get("self").getHref()).contains("r1");
            assertThat(rolls.get(1).getLinks().get("self").getHref()).contains("r2");
        }
    }

    private GameDto gameDto(String id) {
        GameDto game = new GameDto();
        game.setId(id);
        game.setPlayers(new ArrayList<>());
        return game;
    }

    private PlayerDto playerDto(String id) {
        PlayerDto player = new PlayerDto();
        player.setId(id);
        player.setFrames(new ArrayList<>());
        return player;
    }

    private FrameDto frameDto(String id) {
        FrameDto frame = new FrameDto();
        frame.setId(id);
        frame.setRolls(new ArrayList<>());
        return frame;
    }

    private RollDto rollDto(String id) {
        RollDto roll = new RollDto();
        roll.setId(id);
        return roll;
    }

    private GameDto gameDtoWithPlayer(String gameId, String playerId) {
        GameDto game = gameDto(gameId);
        game.setPlayers(new ArrayList<>(List.of(playerDto(playerId))));
        return game;
    }

    private GameDto gameDtoWithFrame(String gameId, String playerId, String frameId) {
        GameDto game = gameDtoWithPlayer(gameId, playerId);
        game.getPlayers().get(0).setFrames(new ArrayList<>(List.of(frameDto(frameId))));
        return game;
    }

    private GameDto gameDtoWithRoll(String gameId, String playerId, String frameId, String rollId) {
        GameDto game = gameDtoWithFrame(gameId, playerId, frameId);
        game.getPlayers().get(0).getFrames().get(0).setRolls(new ArrayList<>(List.of(rollDto(rollId))));
        return game;
    }
}

