package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.util.rolling.RollingAverageTracker;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class SmoothTempoDrawer extends TempoDrawer {
    public static final int DEFAULT_WINDOW = 20;
    private RollingAverageTracker.DoubleArrayTracker tracker;

    public SmoothTempoDrawer(DrawContext context) {
        super(context);

    }

    
    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        updateAudioData(sec, length);
        double[] lagData = getLagData();
        tracker.add(lagData);
        double[] displayData = tracker.getAverage();
        double max = Arrays.stream(displayData).max().orElseThrow();
        for (int i = 0; i < displayData.length; i++) displayData[i] /= max;
        return drawArray(displayData);
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        tracker = new RollingAverageTracker.DoubleArrayTracker(getSetting("window", Integer.class));
    }

    @Override
    public SettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("window", SettingType.positiveInt(), DEFAULT_WINDOW);
    }
}
