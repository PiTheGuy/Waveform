package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;

import java.util.*;

public class DifferenceSpectrogramDrawer extends AbstractSpectrogramDrawer {
    private int order;
    private Deque<double[]> history;

    public DifferenceSpectrogramDrawer(DrawContext context) {
        super(context);
    }

    
    @Override
    protected double[] resample(double[] magnitudes) {
        if (history.size() < order) {
            history.addLast(magnitudes);
            return super.resample(magnitudes);
        }
        double[] pastMagnitudes = history.peekFirst();
        double[] newMagnitudes = new double[magnitudes.length];
        Arrays.setAll(newMagnitudes, i -> magnitudes[i] - pastMagnitudes[i]);
        history.removeFirst();
        history.addLast(magnitudes);
        return super.resample(newMagnitudes);
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        order = getSetting("order", Integer.class);
        history = new ArrayDeque<>(order);
    }

    @Override
    public SettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("order", SettingType.positiveInt(), 1);
    }
}
