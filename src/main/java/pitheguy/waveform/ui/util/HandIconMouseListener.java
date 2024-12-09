package pitheguy.waveform.ui.util;

import pitheguy.waveform.ui.Waveform;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * {@code MouseListener} that shows the hand cursor when hovering over an element.
 */
public class HandIconMouseListener extends MouseAdapter {
    private final Component parent;

    public HandIconMouseListener(Component parent) {
        this.parent = parent;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        parent.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        parent.setCursor(Waveform.getCorrectCursor());
    }
}
