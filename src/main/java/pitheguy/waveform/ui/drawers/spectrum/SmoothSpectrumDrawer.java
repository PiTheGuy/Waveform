package pitheguy.waveform.ui.drawers.spectrum;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.SettingType;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;
import pitheguy.waveform.util.rolling.RollingAverageTracker;

import java.awt.image.BufferedImage;

public class SmoothSpectrumDrawer extends SpectrumDrawer {
    public static final int DEFAULT_WINDOW = 20;
    private RollingAverageTracker.DoubleArrayTracker tracker;

    public SmoothSpectrumDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        updateAudioData(sec, length);
        short[] data = AudioData.averageChannels(left, right);
        double[] magnitudes = FftAnalyser.performFFT(Util.normalize(data));
        tracker.add(magnitudes);
        double[] displayData = tracker.getAverage();
        return drawArray(Util.normalize(displayData), createBlankImage());
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        int window = Config.visualizer.getSettings().getValue("window", Integer.class);
        tracker = new RollingAverageTracker.DoubleArrayTracker(window);
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("window", SettingType.positiveInt(), DEFAULT_WINDOW);
    }
}
