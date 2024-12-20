package pitheguy.waveform.ui.controls;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.ResourceGetter;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class NextTrackButton extends ControlButton {
    private static final ImageIcon ICON = ResourceGetter.getUiIcon("controls/next.png");
    public static final int BUTTON_WIDTH = ICON.getIconWidth();
    public static final int BUTTON_HEIGHT = ICON.getIconHeight();

    private final Waveform parent;

    public NextTrackButton(Waveform parent) {
        super(parent, ICON);
        this.parent = parent;
        getAccessibleContext().setAccessibleName("Next Track");
    }

    @Override
    protected void onClick(ActionEvent e) {
        parent.nextTrack();
    }

    public void updateState() {
        setVisible(shouldBeVisible());
    }

    private boolean shouldBeVisible() {
        if (Config.disableSkipping) return false;
        else return parent.hasNextTrack();
    }
}
