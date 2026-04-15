package de.europace.bowlingbrain.game;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FrameTest {

    @Test
    void shouldDetectStrike() {
        Frame frame = frameWith(10);

        assertThat(frame.isStrike()).isTrue();
        assertThat(frame.isSpare()).isFalse();
    }

    @Test
    void shouldDetectSpare() {
        Frame frame = frameWith(4, 6);

        assertThat(frame.isSpare()).isTrue();
        assertThat(frame.isStrike()).isFalse();
    }

    @Test
    void shouldNotBeSpareOrStrikeForNormalFrame() {
        Frame frame = frameWith(3, 4);

        assertThat(frame.isStrike()).isFalse();
        assertThat(frame.isSpare()).isFalse();
    }

    @Test
    void shouldNotBeSpareWhenFirstRollIsStrike() {
        Frame frame = frameWith(10);

        assertThat(frame.isSpare()).isFalse();
    }

    @Test
    void shouldCalculateTotalPins() {
        assertThat(frameWith(3, 5).totalPins()).isEqualTo(8);
    }

    @Test
    void shouldCalculateTotalPinsForLastFrameWithThreeRolls() {
        assertThat(frameWith(10, 10, 10).totalPins()).isEqualTo(30);
    }

    @Test
    void shouldReturnZeroAndNoFlagsForEmptyFrame() {
        Frame frame = new Frame();

        assertThat(frame.totalPins()).isEqualTo(0);
        assertThat(frame.isStrike()).isFalse();
        assertThat(frame.isSpare()).isFalse();
    }

    @Test
    void shouldDetectGutterBall() {
        Frame frame = frameWith(0, 0);

        assertThat(frame.isStrike()).isFalse();
        assertThat(frame.isSpare()).isFalse();
        assertThat(frame.totalPins()).isEqualTo(0);
    }

    private Frame frameWith(int... pins) {
        Frame frame = new Frame();
        List<Roll> rolls = Arrays.stream(pins).mapToObj(Roll::new).toList();
        frame.setRolls(rolls);
        return frame;
    }
}
