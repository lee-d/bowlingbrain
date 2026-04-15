package de.europace.bowlingbrain.game;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class Frame {

    private String id = UUID.randomUUID().toString();

    @Valid
    @Size(max = 3)
    private List<Roll> rolls = new ArrayList<>();

    private Integer score;

    public boolean isStrike() {
        return !rolls.isEmpty() && rolls.getFirst().getSmashedPins() == 10;
    }

    public boolean isSpare() {
        if (isStrike() || rolls.size() < 2) return false;
        final int firstRollPins = rolls.get(0).getSmashedPins();
        final int secondRollPins = rolls.get(1).getSmashedPins();
        return firstRollPins + secondRollPins == 10;
    }

    public int totalPins() {
        return rolls.stream().mapToInt(Roll::getSmashedPins).sum();
    }
}
