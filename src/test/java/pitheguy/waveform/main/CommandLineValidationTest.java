package pitheguy.waveform.main;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineValidationTest {
    @Test
    void testNoArguments() {
        assertValid();
    }

    @Test
    void testValidVisualizer() {
        assertValid("-visualizer", "spectrogram");
    }

    @Test
    void testNegativeFrameRate() {
        assertInvalid("-frameRate", "-5");
    }

    @Test
    void testInvalidVisualizer() {
        assertInvalid("-visualizer", "abcdefg");
    }

    @Test
    void testInvalidModeInvalidColors() {
        assertAll(() -> assertInvalid("-backgroundColor", "blurple"),
                () -> assertInvalid("-foregroundColor", "blurple"),
                () -> assertInvalid("-playedColor", "blurple"));
    }

    @Test
    void testLoopAndExitOnFinish() {
        assertInvalid("-loop", "-exitOnFinish");
    }

    @Test
    void testFullScreenWithDimensions() {
        assertInvalid("-fullScreen", "-size", "1600", "1000");
    }

    @Test
    void testMultipleInputSources() {
        assertInvalid("-microphone", "-url", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
    }

    @Test
    void testMultipleExportTypes() {
        assertInvalid("-exportImage", "test.png", "-exportVideo", "test.mp4");
    }



    void assertInvalid(String... args) {
        assertThrows(ParseException.class, () -> Main.processInput(args));
    }

    void assertValid(String... args) {
        assertDoesNotThrow(() -> Main.processInput(args));
    }

}