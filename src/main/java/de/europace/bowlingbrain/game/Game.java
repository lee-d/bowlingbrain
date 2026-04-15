package de.europace.bowlingbrain.game;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "games")
public class Game {

    @Id
    private String id;
    private List<Player> players = new ArrayList<>();
    private String currentPlayerId;

}
