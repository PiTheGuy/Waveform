package pitheguy.waveform.ui;

import org.junit.jupiter.api.*;
import pitheguy.waveform.config.Config;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class WaveformMenuBarTest {
    public static final File TEST_FILE = new File("src/test/resources/test.wav");
    Waveform waveform;
    WaveformMenuBar menuBar;

    @BeforeEach
    void setUp() {
        waveform = new Waveform(false);
        waveform.populateMenuBar();
        menuBar = waveform.menuBar;
    }

    @AfterEach
    void tearDown() {
        Config.disableExports = false;
        Config.disableVisualizerSelection = false;
        Config.microphoneMode = false;
        Config.hideControls = false;
        waveform.destroy();
    }

    @Test
    void testFileMenu_microphoneModeHidesFileOptions() {
        Config.microphoneMode = true;
        menuBar.updateState();
        assertAll(
                () -> assertFalse(menuBar.clearQueueItem.isVisible(), "Clear Queue should be hidden in microphone mode"),
                () -> assertFalse(menuBar.manageQueueItem.isVisible(), "Manage Queue should be hidden in microphone mode"),
                () -> assertFalse(menuBar.trackMenu.isVisible(), "Track menu should be hidden in microphone mode")
        );
    }

    @Test
    void testFileMenu_exitButtonTextChanges() {
        waveform.isQueuePanelVisible = true;
        menuBar.updateState();
        assertEquals("Close Queue", menuBar.exitItem.getText(), "Exit item text should be 'Close Queue' when queue panel is visible");
        waveform.isQueuePanelVisible = false;
        menuBar.updateState();
        assertEquals("Exit", menuBar.exitItem.getText(), "Exit item text should be 'Exit' when queue panel is not visible");
    }

    @Test
    void testExportMenu_disableExports() {
        Config.disableExports = true;
        waveform.hasAudio = true;
        menuBar.updateState();
        assertFalse(menuBar.exportMenu.isVisible(), "Export menu should not be visible when exports are disabled");
    }

    @Test
    void testExportMenu_noTrackPlaying() {
        assertFalse(menuBar.exportMenu.isVisible(), "Export menu should not be visible when no track is playing");
    }

    @Test
    void testExportMenu_withTrackPlaying() {
        waveform.hasAudio = true;
        menuBar.updateState();
        assertTrue(menuBar.exportMenu.isVisible(), "Export menu should be visible when a track is playing");
    }


    @Test
    void testVisualizerMenu_noTrackPlaying() {
        assertFalse(menuBar.visualizerMenu.isVisible(), "Visualizer menu should not be visible when no track is playing");
    }

    @Test
    void testVisualizerMenu_withTrackPlaying() {
        waveform.hasAudio = true;
        menuBar.updateState();
        assertTrue(menuBar.visualizerMenu.isVisible(), "Visualizer menu should be visible when a track is playing");
    }

    @Test
    void testVisualizerMenu_disableVisualizerSelection() {
        Config.disableVisualizerSelection = true;
        waveform.hasAudio = true;
        waveform.menuBar.updateState();
        assertFalse(menuBar.visualizerMenu.isVisible(), "Visualizer menu should not be visible when visualizer selection is disabled");
    }

    @Test
    void testTrackMenu_hideControls() {
        Config.hideControls = true;
        waveform.hasAudio = true;
        waveform.menuBar.updateState();
        assertFalse(menuBar.trackMenu.isVisible(), "Track menu should not be visible when controls are hidden from command-line");
        Config.hideControls = false;
    }

    @Test
    void testTrackMenu_pauseButtonTextChanges() throws Exception {
        waveform.play(TEST_FILE);
        assertEquals("Pause", menuBar.pauseItem.getText(), "Pause button should read 'Pause' when track is playing");
        waveform.togglePlayback();
        menuBar.updateState();
        assertEquals("Play", menuBar.pauseItem.getText(), "Pause button should read 'Play' when track is paused");
    }

    @Test
    void testTrackMenu_hideControlsCheckboxReflectsControlVisibility() throws Exception {
        waveform.play(TEST_FILE);
        System.out.println(waveform.controls.isVisible());
        waveform.toggleControls();
        menuBar.updateState();
        assertTrue(menuBar.hideControlsItem.getState(), "Hide controls checkbox should be checked if controls are not visible");
        waveform.toggleControls();
        menuBar.updateState();
        assertFalse(menuBar.hideControlsItem.getState(), "Hide controls checkbox should be unchecked if controls are visible");
    }
}