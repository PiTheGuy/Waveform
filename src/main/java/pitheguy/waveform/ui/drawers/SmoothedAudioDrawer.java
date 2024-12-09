package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.*;
import pitheguy.waveform.util.rolling.RollingAverageTracker;

public abstract class SmoothedAudioDrawer extends AudioDrawer {
    private final int defaultWindow;
    private final boolean addSetting;
    protected RollingAverageTracker.DoubleTracker tracker;

    public SmoothedAudioDrawer(boolean forceFullAudio, int defaultWindow) {
        this(forceFullAudio, defaultWindow, true);
    }

    public SmoothedAudioDrawer(boolean forceFullAudio, int defaultWindow, boolean addSetting) {
        super(forceFullAudio);
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
    public VisualizerSettingsInstance.Builder constructSettings() {
        if (Config.disableSmoothing || !addSetting) return super.constructSettings();
        return super.constructSettings()
                .addSetting("window", SettingType.positiveInt(), defaultWindow);
    }
}