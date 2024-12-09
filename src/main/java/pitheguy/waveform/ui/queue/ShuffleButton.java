package pitheguy.waveform.ui.queue;

import com.google.api.client.repackaged.com.google.common.annotations.VisibleForTesting;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.controls.ControlButton;
import pitheguy.waveform.util.Util;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;

public class ShuffleButton extends ControlButton {
    public static final ImageIcon ICON = new ImageIcon(Waveform.class.getResource("/icons/shuffle.png"));
    public static final int WIDTH = ICON.getIconWidth();
    public static final int HEIGHT = ICON.getIconHeight();

    public ShuffleButton(Waveform parent) {
        super(parent, ICON);
        setToolTipText("<html>Shuffle remaining tracks<br>Hold shift to shuffle everything</html>");
        getAccessibleContext().setAccessibleName("Shuffle");
    }

    @Override
    protected void onClick(ActionEvent e) {
        if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) fullShuffle(parent);
        else shuffle(parent);
    }

    @VisibleForTesting
    static void shuffle(Waveform parent) {
        if (!parent.hasNextTrack()) return;
        Collections.shuffle(parent.getQueue().subList(parent.queueIndex() + 1, parent.queueSize()));
        parent.queuePanel.repopulate();
    }

    @VisibleForTesting
    static void fullShuffle(Waveform parent) {
        if (parent.queueSize() == 0) return;
        Collections.shuffle(parent.getQueue());
        parent.frameUpdater.silentShutdown();
        Util.showErrorOnException(() -> parent.forcePlayIndex(0), "Failed to start playback after shuffling");
        parent.queuePanel.repopulate();
    }
}
