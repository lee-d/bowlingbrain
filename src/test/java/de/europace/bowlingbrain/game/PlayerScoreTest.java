package de.europace.bowlingbrain.game;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerScoreTest {

    @Test
    void allGutterBallsShouldScoreZero() {
        Player player = playerWithRollsPerFrame(
                rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0),
                rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0)
        );

        assertThat(player.calculateScores()).isEqualTo(0);
        assertThat(player.getFrames()).allSatisfy(f -> assertThat(f.getScore()).isNotNull());
    }

    @Test
    void allOnesShouldScoreTwenty() {
        Player player = playerWithRollsPerFrame(
                rollsOf(1, 1), rollsOf(1, 1), rollsOf(1, 1), rollsOf(1, 1), rollsOf(1, 1),
                rollsOf(1, 1), rollsOf(1, 1), rollsOf(1, 1), rollsOf(1, 1), rollsOf(1, 1)
        );

        assertThat(player.calculateScores()).isEqualTo(20);
    }

    @Test
    void singleSpareShouldAddNextRollAsBonus() {
        Player player = playerWithRollsPerFrame(
                rollsOf(5, 5),  // spare -> bonus = next roll
                rollsOf(3, 4),  // next roll = 3, so frame 1 = 10 + 3 = 13
                rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0),
                rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0)
        );

        assertThat(player.calculateScores()).isEqualTo(13 + 7);
        assertThat(player.getFrames().get(0).getScore()).isEqualTo(13);
        assertThat(player.getFrames().get(1).getScore()).isEqualTo(20);
    }

    @Test
    void singleStrikeShouldAddNextTwoRollsAsBonus() {
        Player player = playerWithRollsPerFrame(
                rollsOf(10),    // strike -> bonus = next 2 rolls
                rollsOf(3, 4),  // next 2 rolls = 3+4, so frame 1 = 10 + 3 + 4 = 17
                rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0),
                rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0)
        );

        assertThat(player.calculateScores()).isEqualTo(17 + 7);
        assertThat(player.getFrames().get(0).getScore()).isEqualTo(17);
        assertThat(player.getFrames().get(1).getScore()).isEqualTo(24);
    }

    @Test
    void consecutiveStrikesBonusShouldSpanTwoFrames() {
        Player player = playerWithRollsPerFrame(
                rollsOf(10),    // frame 1: 10 + 10 + 3 = 23, cumulative: 23
                rollsOf(10),    // frame 2: 10 + 3 + 4 = 17, cumulative: 40
                rollsOf(3, 4),  // frame 3: 7,              cumulative: 47
                rollsOf(0, 0), rollsOf(0, 0),
                rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0)
        );

        player.calculateScores();

        assertThat(player.getFrames().get(0).getScore()).isEqualTo(23);
        assertThat(player.getFrames().get(1).getScore()).isEqualTo(40);
        assertThat(player.getFrames().get(2).getScore()).isEqualTo(47);
    }

    @Test
    void perfectGameShouldScoreThreeHundred() {
        Player player = playerWithRollsPerFrame(
                rollsOf(10), rollsOf(10), rollsOf(10), rollsOf(10), rollsOf(10),
                rollsOf(10), rollsOf(10), rollsOf(10), rollsOf(10), rollsOf(10, 10, 10)
        );

        assertThat(player.calculateScores()).isEqualTo(300);
    }

    @Test
    void allFiveSparesShouldScoreOneFifty() {
        Player player = playerWithRollsPerFrame(
                rollsOf(5, 5), rollsOf(5, 5), rollsOf(5, 5), rollsOf(5, 5), rollsOf(5, 5),
                rollsOf(5, 5), rollsOf(5, 5), rollsOf(5, 5), rollsOf(5, 5), rollsOf(5, 5, 5)
        );

        assertThat(player.calculateScores()).isEqualTo(150);
    }

    @Test
    void tenthFrameSpareAllowsExtraRollAndNoBonus() {
        Player player = playerWithRollsPerFrame(
                rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0),
                rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(5, 5, 3)
        );

        assertThat(player.calculateScores()).isEqualTo(13);
    }

    @Test
    void tenthFrameStrikeAllowsTwoExtraRollsAndNoBonus() {
        Player player = playerWithRollsPerFrame(
                rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0),
                rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(10, 7, 2)
        );

        assertThat(player.calculateScores()).isEqualTo(19);
    }

    @Test
    void cumulativeFrameScoresShouldBeCorrect() {
        Player player = playerWithRollsPerFrame(
                rollsOf(3, 4),  //  7 cumulative:  7
                rollsOf(5, 5),  // spare: 10+6=16, cumulative: 23
                rollsOf(6, 1),  //  7 cumulative: 30
                rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0),
                rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0), rollsOf(0, 0)
        );

        player.calculateScores();

        assertThat(player.getFrames().get(0).getScore()).isEqualTo(7);
        assertThat(player.getFrames().get(1).getScore()).isEqualTo(23);
        assertThat(player.getFrames().get(2).getScore()).isEqualTo(30);
    }

    @Test
    void frameScoresShouldBeNullBeforeCalculation() {
        Player player = playerWithRollsPerFrame(rollsOf(3, 4));

        assertThat(player.getFrames().getFirst().getScore()).isNull();
    }

    private Player playerWithRollsPerFrame(List<Roll>... framesRolls) {
        Player player = new Player("Test");
        List<Frame> frames = Arrays.stream(framesRolls)
                .map(rolls -> {
                    Frame frame = new Frame();
                    frame.setRolls(rolls);
                    return frame;
                })
                .toList();
        player.setFrames(frames);
        return player;
    }

    private List<Roll> rollsOf(int... pins) {
        return IntStream.of(pins).mapToObj(Roll::new).toList();
    }
}
