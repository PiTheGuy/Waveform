package pitheguy.waveform.ui.drawers.waveform;

import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.io.DrawContext;

import java.util.ArrayDeque;

public class RollingAverageWaveformDrawer extends ResampledWaveformDrawer {
    public static final int DEFAULT_WINDOW = 50;

    public RollingAverageWaveformDrawer(DrawContext context) {
        super(context);
    }

    @Override
    public short[] resample(short[] data) {
        short[] resampled = new short[data.length];
        int window = getSetting("window", Integer.class);
        ArrayDeque<Short> previousSamples = new ArrayDeque<>();
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            previousSamples.addLast(data[i]);
            sum += data[i];
            if (previousSamples.size() > window) sum -= previousSamples.removeFirst();
            resampled[i] = (short) (sum / previousSamples.size());
        }
        return resampled;
    }

    @Override
    public SettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("window", "Smoothing window", SettingType.positiveInt(), DEFAULT_WINDOW);
    }
}
