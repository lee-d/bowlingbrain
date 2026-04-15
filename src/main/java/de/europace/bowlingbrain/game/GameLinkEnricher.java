package de.europace.bowlingbrain.game;

import de.europace.bowlingbrain.api.dto.FrameDto;
import de.europace.bowlingbrain.api.dto.GameDto;
import de.europace.bowlingbrain.api.dto.LinkDto;
import de.europace.bowlingbrain.api.dto.PlayerDto;
import de.europace.bowlingbrain.api.dto.RollDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GameLinkEnricher {

    private static final String BASE = "/api/v1/games";

    public GameDto enrich(GameDto game) {
        final String gameUrl = BASE + "/" + game.getId();
        game.setLinks(gameLinks(game, gameUrl));
        if (game.getPlayers() != null) {
            game.getPlayers().forEach(player -> enrichPlayer(player, gameUrl));
        }
        return game;
    }

    private Map<String, LinkDto> gameLinks(GameDto game, String gameUrl) {
        final Map<String, LinkDto> links = new HashMap<>();
        links.put("self", link(gameUrl));
        links.put("next-player", link(gameUrl + "/current-player/next"));
        links.put("add-player", link(gameUrl + "/players"));
        return links;
    }

    private void enrichPlayer(PlayerDto player, String gameUrl) {
        final String playerUrl = gameUrl + "/players/" + player.getId();
        final Map<String, LinkDto> links = new HashMap<>();
        links.put("self", link(playerUrl));
        links.put("add-frame", link(playerUrl + "/frames"));
        player.setLinks(links);
        if (player.getFrames() != null) {
            player.getFrames().forEach(frame -> enrichFrame(frame, playerUrl));
        }
    }

    private void enrichFrame(FrameDto frame, String playerUrl) {
        final String frameUrl = playerUrl + "/frames/" + frame.getId();
        final Map<String, LinkDto> links = new HashMap<>();
        links.put("self", link(frameUrl));
        links.put("add-roll", link(frameUrl + "/rolls"));
        frame.setLinks(links);
        if (frame.getRolls() != null) {
            frame.getRolls().forEach(roll -> enrichRoll(roll, frameUrl));
        }
    }

    private void enrichRoll(RollDto roll, String frameUrl) {
        final Map<String, LinkDto> links = new HashMap<>();
        links.put("self", link(frameUrl + "/rolls/" + roll.getId()));
        roll.setLinks(links);
    }

    private LinkDto link(String href) {
        final LinkDto linkDto = new LinkDto();
        linkDto.setHref(href);
        return linkDto;
    }
}
