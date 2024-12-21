package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.util.rolling.RollingAverageTracker;

public abstract class SmoothedAudioDrawer extends AudioDrawer {
    private final int defaultWindow;
    private final boolean addSetting;
    protected RollingAverageTracker.DoubleTracker tracker;

    public SmoothedAudioDrawer(DrawContext context, int defaultWindow) {
        this(context, defaultWindow, true);
    }

    public SmoothedAudioDrawer(DrawContext context, int defaultWindow, boolean addSetting) {
        super(context);
        this.defaultWindow = defaultWindow;
        this.addSetting = addSetting;
    }

    protected double getDisplayValue(double value) {
        if (Config.disableSmoothing) return value;
        tracker.add(value);
        return tracker.getAverage();
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        tracker = new RollingAverageTracker.DoubleTracker(getWindow());
    }

    private int getWindow() {
        if (!addSetting) return defaultWindow;
        return getSetting("window", Integer.class);
    }

    @Override
    public SettingsInstance.Builder constructSettings() {
        if (Config.disableSmoothing || !addSetting) return super.constructSettings();
        return super.constructSettings()
                .addSetting("window", SettingType.positiveInt(), defaultWindow);
    }
}
