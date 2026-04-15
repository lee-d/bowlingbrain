package de.europace.bowlingbrain.game;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class Roll {

    public static final int MAX_SCORE_PER_ROLL = 10;
    private String id = UUID.randomUUID().toString();

    @Min(0)
    @Max(MAX_SCORE_PER_ROLL)
    private int smashedPins;

    public Roll(final int smashedPins) {
        this.smashedPins = smashedPins;
    }
}
