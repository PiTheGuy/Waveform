package pitheguy.waveform.config;

import pitheguy.waveform.ui.Waveform;

import javax.swing.*;

public enum LoopState {
    OFF("/icons/loop_off.png"),
    ALL("/icons/loop_on.png"),
    TRACk("/icons/loop_track.png"),;

    private final ImageIcon icon;

    LoopState(String iconPath) {
        icon = new ImageIcon(Waveform.class.getResource(iconPath));
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public LoopState next() {
        return switch (this) {
            case OFF -> ALL;
            case ALL -> TRACk;
            case TRACk -> OFF;
        };
    }
}
