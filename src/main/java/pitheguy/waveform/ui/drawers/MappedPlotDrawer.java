package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.VisualizerSettingsInstance;

public abstract class MappedPlotDrawer extends AudioDrawer {
    public short maxValue = Short.MAX_VALUE;

    public MappedPlotDrawer(DrawContext context) {
        super(context);
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        updateMaxValue();
    }

    protected void updateMaxValue() {
        if (!getSetting("relative", Boolean.class)) return;
        short maxValue = playingAudio.left()[0];
        for (int i = 1; i < playingAudio.left().length; i++) if (playingAudio.left()[i] > maxValue) maxValue = playingAudio.left()[i];
        for (int i = 0; i < playingAudio.right().length; i++) if (playingAudio.right()[i] > maxValue) maxValue = playingAudio.right()[i];
        this.maxValue = maxValue;
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("relative", "Relative scaling", SettingType.bool(), false);
    }
}
