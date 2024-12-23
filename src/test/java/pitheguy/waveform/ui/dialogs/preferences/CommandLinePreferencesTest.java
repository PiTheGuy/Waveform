package pitheguy.waveform.ui.dialogs.preferences;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;
import pitheguy.waveform.main.WaveColor;

import static org.junit.jupiter.api.Assertions.*;

class CommandLinePreferencesTest {
    @Test
    void testFromCommandLine_oneOption() throws ParseException {
        CommandLinePreferences commandLinePreferences = CommandLinePreferences.builderFromString("foregroundColor=blue").build();
        assertTrue(commandLinePreferences.foregroundColor().isPresent());
        assertEquals(WaveColor.BLUE.getColor(), commandLinePreferences.foregroundColor().get());
    }

    @Test
    void testFromCommandLine_twoOptions() throws ParseException {
        CommandLinePreferences commandLinePreferences = CommandLinePreferences.builderFromString("foregroundColor=blue,backgroundColor=black").build();
        assertTrue(commandLinePreferences.foregroundColor().isPresent());
        assertTrue(commandLinePreferences.backgroundColor().isPresent());
        assertEquals(WaveColor.BLUE.getColor(), commandLinePreferences.foregroundColor().get());
        assertEquals(WaveColor.BLACK.getColor(), commandLinePreferences.backgroundColor().get());
    }

    @Test
    void testFromCommandLine_boolean() throws ParseException {
        CommandLinePreferences commandLinePreferences = CommandLinePreferences.builderFromString("dynamicIcon=false").build();
        assertTrue(commandLinePreferences.dynamicIcon().isPresent());
        assertEquals(false, commandLinePreferences.dynamicIcon().get());
    }

    @Test
    void testFromCommandLine_null() throws ParseException {
        CommandLinePreferences commandLinePreferences = CommandLinePreferences.builderFromString(null).build();
        assertEquals(CommandLinePreferences.EMPTY, commandLinePreferences);
    }

    @Test
    void testFromCommandLine_invalidOption() {
        assertThrows(ParseException.class, () -> CommandLinePreferences.builderFromString("invalidOption=value"));
    }

    @Test
    void testFromCommandLine_duplicateOption() {
        assertThrows(ParseException.class, () -> CommandLinePreferences.builderFromString("foregroundColor=blue,foregroundColor=red"));
    }

    @Test
    void testFromCommandLine_invalidColor() {
        assertThrows(ParseException.class, () -> CommandLinePreferences.builderFromString("foregroundColor=invalidColor"));
    }

    @Test
    void testFromCommandLine_invalidBoolean() {
        assertThrows(ParseException.class, () -> CommandLinePreferences.builderFromString("dynamicIcon=maybe"));
    }
}