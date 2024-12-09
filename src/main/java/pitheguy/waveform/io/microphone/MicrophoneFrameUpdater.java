package pitheguy.waveform.io.microphone;

import pitheguy.waveform.ui.FrameUpdater;
import pitheguy.waveform.ui.Waveform;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class MicrophoneFrameUpdater extends FrameUpdater {
    public MicrophoneFrameUpdater(Consumer<Double> task, Waveform parent, ScheduledExecutorService scheduler) {
        super(task, parent);
    }

    @Override
    public double getSec() {
        return 0.0;
    }

    @Override
    protected boolean isClipOver() {
        return false;
    }
}
