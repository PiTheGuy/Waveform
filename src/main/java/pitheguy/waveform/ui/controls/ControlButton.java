package pitheguy.waveform.ui.controls;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.util.HandIconMouseListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public abstract class ControlButton extends JButton {
    protected final Waveform parent;

    public ControlButton(Waveform parent, ImageIcon icon) {
        super(icon);
        this.parent = parent;
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
        addActionListener(this::onClick);
        addMouseListener(new HandIconMouseListener(this));
    }

    protected abstract void onClick(ActionEvent e);
}
