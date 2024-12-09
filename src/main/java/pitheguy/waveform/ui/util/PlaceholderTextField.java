package pitheguy.waveform.ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class PlaceholderTextField extends JTextField {
    private final String placeholder;

    public PlaceholderTextField(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && placeholder != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.GRAY);
            Insets insets = getInsets();
            int x = insets.left + 2;
            int y = getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - g2.getFontMetrics().getDescent();
            g2.drawString(placeholder, x, y);
            g2.dispose();
        }
    }
}
