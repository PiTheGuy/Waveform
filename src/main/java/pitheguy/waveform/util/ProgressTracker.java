package pitheguy.waveform.util;

import pitheguy.waveform.ui.ProgressWindow;
import pitheguy.waveform.ui.Waveform;

public interface ProgressTracker {
    void step();

    void setText(String text);

    default void finish() {
    }

    static ProgressTracker getProgressTracker(Waveform waveform, String message, int maxValue) {
        if (waveform.isVisible()) return new ProgressWindow(waveform, message, maxValue);
        else return new CommandLineProgressTracker(message, maxValue);
    }
}
