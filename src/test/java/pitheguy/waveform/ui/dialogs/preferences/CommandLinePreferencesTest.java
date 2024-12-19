package pitheguy.waveform.ui.dialogs.preferences;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;
import pitheguy.waveform.main.WaveColor;

import static org.junit.jupiter.api.Assertions.*;

class CommandLinePreferencesTest {
    @Test
    void testFromString_oneOption() throws ParseException {
        CommandLinePreferences commandLinePreferences = CommandLinePreferences.fromString("foregroundColor=blue");
        assertTrue(commandLinePreferences.foregroundColor().isPresent());
        assertEquals(WaveColor.BLUE.getColor(), commandLinePreferences.foregroundColor().get());
    }

    @Test
    void testFromString_twoOptions() throws ParseException {
        CommandLinePreferences commandLinePreferences = CommandLinePreferences.fromString("foregroundColor=blue,backgroundColor=black");
        assertTrue(commandLinePreferences.foregroundColor().isPresent());
        assertTrue(commandLinePreferences.backgroundColor().isPresent());
        assertEquals(WaveColor.BLUE.getColor(), commandLinePreferences.foregroundColor().get());
        assertEquals(WaveColor.BLACK.getColor(), commandLinePreferences.backgroundColor().get());
    }

    @Test
    void testFromString_boolean() throws ParseException {
        CommandLinePreferences commandLinePreferences = CommandLinePreferences.fromString("dynamicIcon=false");
        assertTrue(commandLinePreferences.dynamicIcon().isPresent());
        assertEquals(false, commandLinePreferences.dynamicIcon().get());
    }

    @Test
    void testFromString_null() throws ParseException {
        CommandLinePreferences commandLinePreferences = CommandLinePreferences.fromString(null);
        assertEquals(CommandLinePreferences.EMPTY, commandLinePreferences);
    }

    @Test
    void testFromString_invalidOption() {
        assertThrows(ParseException.class, () -> CommandLinePreferences.fromString("invalidOption=value"));
    }

    @Test
    void testFromString_duplicateOption() {
        assertThrows(ParseException.class, () -> CommandLinePreferences.fromString("foregroundColor=blue,foregroundColor=red"));
    }

    @Test
    void testFromString_invalidColor() {
        assertThrows(ParseException.class, () -> CommandLinePreferences.fromString("foregroundColor=invalidColor"));
    }

    @Test
    void testFromString_invalidBoolean() {
        assertThrows(ParseException.class, () -> CommandLinePreferences.fromString("dynamicIcon=maybe"));
    }
}