package pitheguy.waveform.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GuiControllerTest {
    @Test
    public void testToggleQueuePanel() {
        Waveform waveform = new Waveform(false);
        GuiController controller = waveform.controller;
        assertFalse(controller.isQueuePanelVisible());
        assertFalse(controller.getQueuePanel().isVisible());
        controller.toggleQueuePanel();
        assertTrue(controller.isQueuePanelVisible());
        assertTrue(controller.getQueuePanel().isVisible());
        waveform.destroy();
    }

}