package pitheguy.waveform.ui.util;

import pitheguy.waveform.ui.Waveform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

public class ClickableMouseListener extends MouseAdapter {
    private final JComponent parent;
    private final Supplier<Color> backgroundColor;
    private final Color hoverColor;
    private Runnable action;

    public ClickableMouseListener(JComponent parent, Color backgroundColor, Color hoverColor, Runnable action) {
        this(parent, () -> backgroundColor, hoverColor, action);
    }

    public ClickableMouseListener(JComponent parent, Supplier<Color> backgroundColor, Color hoverColor, Runnable action) {
        this.parent = parent;
        this.backgroundColor = backgroundColor;
        this.hoverColor = hoverColor;
        this.action = action;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(e)) action.run();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        parent.setBackground(hoverColor);
        parent.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        parent.setBackground(backgroundColor.get());
        parent.setCursor(Waveform.getCorrectCursor());
    }
}
