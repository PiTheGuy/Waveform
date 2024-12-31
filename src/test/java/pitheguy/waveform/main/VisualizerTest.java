package pitheguy.waveform.main;

import org.junit.jupiter.api.Test;
import pitheguy.waveform.ui.Waveform;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VisualizerTest {
    @Test
    void testGetVisualizers() {
        Visualizer[] results = Visualizer.getVisualizers(false);
        for (Visualizer result : results) assertFalse(result.isCommandLineOnly());
    }

    @Test
    void testGetVisualizers_includeCommandLineOnly() {
        List<Visualizer> results = Arrays.asList(Visualizer.getVisualizers(true));
        for (Visualizer v : Visualizer.values()) assertTrue(results.contains(v));
    }

    @Test
    void testVisualizers() {
        Waveform waveform = new Waveform(false);
        waveform.controller.populateMenuBar();
        for (Visualizer v : Visualizer.getVisualizers(false))
            assertDoesNotThrow(() -> waveform.switchVisualizer(v), "Failed to switch to " + v.getName());
    }
}