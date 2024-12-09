package pitheguy.waveform.ui.queue;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.config.LoopState;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.controls.ControlButton;

import java.awt.event.ActionEvent;

public class LoopButton extends ControlButton {
    public static final int WIDTH = LoopState.OFF.getIcon().getIconWidth();
    public static final int HEIGHT = LoopState.OFF.getIcon().getIconHeight();

    public LoopButton(Waveform parent) {
        super(parent, Config.loop.getIcon());
        setToolTipText("Loop");
        getAccessibleContext().setAccessibleName("Loop");
    }

    @Override
    protected void onClick(ActionEvent e) {
        Config.loop = Config.loop.next();
        setIcon(Config.loop.getIcon());
    }
}
