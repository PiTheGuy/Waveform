
package pitheguy.waveform.ui.dialogs.preferences;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import pitheguy.waveform.ui.Waveform;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class PreferencesDialogTest {

    @Test
    void testSystemTray_disabledWhenUnsupported() {
        try (MockedStatic<SystemTray> controller = mockStatic(SystemTray.class)) {
            controller.when(SystemTray::isSupported).thenReturn(false);
            Waveform waveform = new Waveform(false);
            PreferencesDialog dialog = new PreferencesDialog(waveform);
            assertFalse(dialog.showInSystemTray.isEnabled());
            assertFalse(dialog.notifications.isEnabled());
        }
    }
}