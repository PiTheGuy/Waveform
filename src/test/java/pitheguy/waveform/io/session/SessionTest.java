package pitheguy.waveform.io.session;

import org.junit.jupiter.api.Test;
import pitheguy.waveform.config.NotificationState;
import pitheguy.waveform.config.SavedPreferences;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SessionTest {
    public static final File SESSION_FILE = new File("session.json");

    @Test
    void testSaveCreatesFile() throws IOException {
        Map<String, Object> map = Map.of("backgroundColor", Color.GREEN, "foregroundColor", Color.BLUE, "dynamicIcon", false);
        SavedPreferences preferences = SavedPreferences.create(map);
        Session session = new Session(preferences, List.of(), List.of());
        session.save(SESSION_FILE);
        assertTrue(SESSION_FILE.exists());
        Files.delete(SESSION_FILE.toPath());
    }

    @Test
    void testSaveAndLoadPreferences() throws IOException {
        Map<String, Object> map = Map.ofEntries(
                Map.entry("backgroundColor", Color.GREEN),
                Map.entry("foregroundColor", Color.BLUE),
                Map.entry("playedColor", Color.ORANGE),
                Map.entry("dynamicIcon", false),
                Map.entry("highContrast", true),
                Map.entry("notifications", NotificationState.ALWAYS),
                Map.entry("mono", false),
                Map.entry("disableSmoothing", true),
                Map.entry("showInSystemTray", true),
                Map.entry("pauseOnExport", true),
                Map.entry("forceRead", false)
        );
        SavedPreferences preferences = SavedPreferences.create(map);
        Session session = new Session(preferences, List.of(), List.of());
        session.save(SESSION_FILE);
        SavedPreferences loadedPreferences = Session.load(SESSION_FILE).savedPreferences();
        assertEquals(preferences, loadedPreferences);
        Files.delete(SESSION_FILE.toPath());
    }

    @Test
    void testLoad_nonExistentFile() {
        SavedPreferences preferences = Session.load(SESSION_FILE).savedPreferences();
        assertEquals(SavedPreferences.EMPTY, preferences);
    }

}