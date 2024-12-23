package pitheguy.waveform.ui;

import org.junit.jupiter.api.Test;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.download.YoutubeAudioGetter;
import pitheguy.waveform.main.Visualizer;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class WaveformTest {
    public static final File TEST_FILE = new File("src/test/resources/test.wav");

    @Test
    public void testVisibleParameter_true() {
        Waveform waveform = new Waveform(true);
        assertTrue(waveform.isVisible());
        waveform.destroy();
    }

    @Test
    public void testVisibleParameter_false() {
        Waveform waveform = new Waveform(false);
        assertFalse(waveform.isVisible());
        waveform.destroy();
    }

    @Test
    public void testEmptyState() {
        Waveform waveform = new Waveform(false);
        assertFalse(waveform.hasAudio);
        assertTrue(waveform.isResizable());
        assertEquals(0, waveform.queueIndex());
        assertEquals(Waveform.DRAG_AND_DROP_TEXT, waveform.getText());
        assertEquals(Waveform.STATIC_ICON, waveform.getIconImage());
        waveform.destroy();
    }

    @Test
    public void testPlay_withValidFile() throws Exception {
        Waveform waveform = new Waveform(false);
        waveform.play(TEST_FILE);
        assertEquals("", waveform.getText());
        assertTrue(waveform.hasAudio);
        waveform.destroy();
    }

    @Test
    public void testSwitchVisualizer() throws Exception {
        Waveform waveform = new Waveform(false);
        waveform.populateMenuBar();
        waveform.play(TEST_FILE);
        waveform.switchVisualizer(Visualizer.VALUE_FREQUENCY_HEATMAP);
        assertEquals(Visualizer.VALUE_FREQUENCY_HEATMAP, Config.visualizer);
        assertFalse(waveform.isResizable());
        assertEquals(Waveform.STATIC_ICON, waveform.getIconImage());
        waveform.destroy();
    }

    @Test
    public void testSwitchVisualizer_noAudio() {
        Waveform waveform = new Waveform(false);
        waveform.populateMenuBar();
        waveform.switchVisualizer(Visualizer.EDGE_WAVEFORM);
        assertEquals(Visualizer.EDGE_WAVEFORM, Config.visualizer);
        waveform.destroy();
    }

    @Test
    public void testSwitchVisualizer_commandLineOnly() throws Exception {
        Waveform waveform = new Waveform(false);
        waveform.play(TEST_FILE);
        assertThrows(IllegalArgumentException.class, () -> waveform.switchVisualizer(Visualizer.CHROMAGRAM));
        waveform.destroy();
    }

    @Test
    public void testTogglePlayback() throws Exception {
        Waveform waveform = new Waveform(false);
        waveform.play(TEST_FILE);
        assertFalse(waveform.isPaused());
        waveform.togglePlayback();
        assertTrue(waveform.isPaused());
        waveform.destroy();
    }

    @Test
    public void testIsFileSupported_withValidFile() {
        File audioFile = new File("valid.wav");
        assertTrue(Waveform.isFileSupported(audioFile));
    }

    @Test
    public void testIsFileSupported_withInvalidFile() {
        File textFile = new File("test.txt");
        assertFalse(Waveform.isFileSupported(textFile));
    }

    @Test
    public void testIsFileSupported_withForceRead() {
        Config.settings.setValue("forceRead", true);
        File textFile = new File("test.txt");
        assertTrue(Waveform.isFileSupported(textFile));
        Config.settings.setValue("forceRead", false);
    }

    @Test
    public void testToggleQueuePanel() {
        Waveform waveform = new Waveform(false);
        assertFalse(waveform.isQueuePanelVisible);
        assertFalse(waveform.queuePanel.isVisible());
        waveform.toggleQueuePanel();
        assertTrue(waveform.isQueuePanelVisible);
        assertTrue(waveform.queuePanel.isVisible());
        waveform.destroy();
    }

    @Test
    public void testValidateImport_withInvalidUrl() {
        String invalidUrl = "invalid";
        assertNull(YoutubeAudioGetter.validateUrl(invalidUrl, error -> {}));
    }

    @Test
    public void testValidateImport_withOutsideUrl() {
        String invalidUrl = "https://www.invalid-url.com";
        assertNull(YoutubeAudioGetter.validateUrl(invalidUrl, error -> {}));
    }

    @Test
    public void testValidateImport_withValidYoutubeUrl() {
        String youtubeUrl = "https://www.youtube.com/watch?v=example";
        assertNotNull(YoutubeAudioGetter.validateUrl(youtubeUrl, error -> {}));
    }

    @Test
    public void testValidateImport_withShortYoutubeUrl() {
        String youtubeUrl = "https://youtu.be/example";
        assertNotNull(YoutubeAudioGetter.validateUrl(youtubeUrl, error -> {}));
    }
}