package pitheguy.waveform.ui.controls;

import pitheguy.waveform.ui.Waveform;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PauseButton extends ControlButton {
    private static final ImageIcon PAUSE_ICON = new ImageIcon(Waveform.class.getResource("/icons/controls/pause.png"));
    private static final ImageIcon PLAY_ICON = new ImageIcon(Waveform.class.getResource("/icons/controls/play.png"));
    public static final int BUTTON_WIDTH = PAUSE_ICON.getIconWidth();
    public static final int BUTTON_HEIGHT = PAUSE_ICON.getIconHeight();


    private final Waveform parent;

    public PauseButton(Waveform parent) {
        super(parent, PLAY_ICON);
        this.parent = parent;
        getAccessibleContext().setAccessibleName("Pause");
    }

    protected void onClick(ActionEvent e) {
        parent.togglePlayback();
    }

    public void updateState() {
        if (parent.isPaused()) {
            setIcon(PLAY_ICON);
            getAccessibleContext().setAccessibleName("Play");
        } else {
            setIcon(PAUSE_ICON);
            getAccessibleContext().setAccessibleName("Pause");
        }
    }
}
