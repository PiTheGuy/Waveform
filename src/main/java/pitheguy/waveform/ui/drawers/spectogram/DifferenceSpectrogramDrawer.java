package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.SettingType;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;

import java.util.*;

public class DifferenceSpectrogramDrawer extends AbstractSpectrogramDrawer {
    private int order;
    private Deque<double[]> history;

    public DifferenceSpectrogramDrawer(boolean forceFullAudio) {
        super(forceFullAudio);


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
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("order", SettingType.positiveInt(), 1);
    }
}
