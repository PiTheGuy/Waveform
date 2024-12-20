package pitheguy.waveform.config;

import pitheguy.waveform.util.ResourceGetter;

import javax.swing.*;

public enum LoopState {
    OFF("loop_off.png"),
    ALL("loop_on.png"),
    TRACk("loop_track.png"),;

    private final ImageIcon icon;

    LoopState(String iconPath) {
        icon = ResourceGetter.getUiIcon(iconPath);
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
