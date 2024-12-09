package pitheguy.waveform.ui.dialogs.preferences;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;
import pitheguy.waveform.main.Main;
import pitheguy.waveform.ui.Waveform;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class PreferencesDialogTest {

    @Test
    void testForcedBackgroundColor() throws ParseException {
        Main.processInput("-backgroundColor", "green");
        Waveform waveform = new Waveform(false);
        PreferencesDialog dialog = new PreferencesDialog(waveform);
        assertFalse(dialog.backgroundColor.isEnabled());
        assertEquals(Color.GREEN, dialog.backgroundColor.getColor());
        waveform.destroy();
    }

    @Test
    void testForcedForegroundColor() throws ParseException {
        Main.processInput("-foregroundColor", "red");
        Waveform waveform = new Waveform(false);
        PreferencesDialog dialog = new PreferencesDialog(waveform);
        assertFalse(dialog.foregroundColor.isEnabled());
        assertEquals(Color.RED, dialog.foregroundColor.getColor());
        waveform.destroy();
    }

    @Test
    void testForcedPlayedColor() throws ParseException {
        Main.processInput("-playedColor", "blue");
        Waveform waveform = new Waveform(false);
        PreferencesDialog dialog = new PreferencesDialog(waveform);
        assertFalse(dialog.playedColor.isEnabled());
        assertEquals(Color.BLUE, dialog.playedColor.getColor());
        waveform.destroy();
    }

    @Test
    void testDisabledDynamicIcon() throws ParseException {
        Main.processInput("-disableDynamicIcon");
        Waveform waveform = new Waveform(false);
        PreferencesDialog dialog = new PreferencesDialog(waveform);
        assertFalse(dialog.dynamicIcon.isEnabled());
        assertFalse(dialog.dynamicIcon.isSelected());
        waveform.destroy();
    }

    @Test
    void testForcedHighContrast() throws ParseException {
        Main.processInput("-highContrast");
        Waveform waveform = new Waveform(false);
        PreferencesDialog dialog = new PreferencesDialog(waveform);
        assertFalse(dialog.highContrast.isEnabled());
        assertTrue(dialog.highContrast.isSelected());
        waveform.destroy();
    }
}