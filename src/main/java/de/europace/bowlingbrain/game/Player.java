package de.europace.bowlingbrain.game;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class Player {

    private String id = UUID.randomUUID().toString();

    @NotBlank
    private String name;

    @Valid
    @Size(max = 10)
    private List<Frame> frames = new ArrayList<>();

    public Player(String name) {
        this.name = name;
    }

    public int calculateScores() {
        final List<Roll> allRolls = flattenRolls();
        int totalScore = 0;
        int rollIndex = 0;

        for (int i = 0; i < Math.min(frames.size(), 10); i++) {
            final Frame frame = frames.get(i);
            final int frameScore = isLastFrame(i)
                    ? frame.totalPins()
                    : scoreForFrame(frame, allRolls, rollIndex);

            totalScore += frameScore;
            frame.setScore(totalScore);
            rollIndex += rollsConsumed(frame, i);
        }

        return totalScore;
    }

    private int scoreForFrame(Frame frame, List<Roll> allRolls, int rollIndex) {
        if (frame.isStrike()) {
            return Roll.MAX_SCORE_PER_ROLL + bonusPins(allRolls, rollIndex + 1) + bonusPins(allRolls, rollIndex + 2);
        }
        if (frame.isSpare()) {
            return Roll.MAX_SCORE_PER_ROLL + bonusPins(allRolls, rollIndex + 2);
        }
        return frame.totalPins();
    }

    private int rollsConsumed(Frame frame, int frameIndex) {
        if (isLastFrame(frameIndex)) return frame.getRolls().size();
        if (frame.isStrike()) return 1;
        return 2;
    }

    private boolean isLastFrame(int frameIndex) {
        return frameIndex == 9;
    }

    private List<Roll> flattenRolls() {
        return frames.stream()
                .flatMap(frame -> frame.getRolls().stream())
                .toList();
    }

    private int bonusPins(List<Roll> allRolls, int index) {
        return index < allRolls.size() ? allRolls.get(index).getSmashedPins() : 0;
    }
}
