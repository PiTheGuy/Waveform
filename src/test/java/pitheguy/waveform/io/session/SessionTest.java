package pitheguy.waveform.io.session;

import org.junit.jupiter.api.Test;
import pitheguy.waveform.config.NotificationState;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SessionTest {
    public static final File SESSION_FILE = new File("session.json");

    @Test
    void testSaveCreatesFile() throws IOException {
        SavedPreferences preferences = SavedPreferences.create(Color.ORANGE, Color.GREEN, Color.BLUE, false, true, true, NotificationState.WHEN_MINIMIZED, false, false, true);
        Session session = new Session(preferences, List.of(), List.of());
        session.save(SESSION_FILE);
        assertTrue(SESSION_FILE.exists());
        Files.delete(SESSION_FILE.toPath());
    }

    @Test
    void testSaveAndLoadPreferences() throws IOException {
        SavedPreferences preferences = SavedPreferences.create(Color.RED, Color.GREEN, Color.BLUE, false, true, true, NotificationState.WHEN_MINIMIZED, false, false, true);
        Session session = new Session(preferences, List.of(), List.of());
        session.save(SESSION_FILE);
        SavedPreferences loadedPreferences = Session.load(SESSION_FILE).savedPreferences();
        assertEquals(preferences, loadedPreferences);
        Files.delete(SESSION_FILE.toPath());
    }

    @Test
    void testLoad_invalidFile() {
        File file = new File("src/test/resources/bad_preferences.json");
        assumeTrue(file.exists(), "Missing malformed preferences file");
        SavedPreferences preferences = Session.load(file).savedPreferences();
        assertTrue(preferences.backgroundColor().isEmpty());
        assertTrue(preferences.foregroundColor().isPresent());
        assertTrue(preferences.playedColor().isEmpty());
        assertTrue(preferences.dynamicIcon().isEmpty());
    }

    @Test
    void testLoad_nonExistentFile() {
        SavedPreferences preferences = Session.load(SESSION_FILE).savedPreferences();
        assertEquals(SavedPreferences.EMPTY, preferences);
    }

}