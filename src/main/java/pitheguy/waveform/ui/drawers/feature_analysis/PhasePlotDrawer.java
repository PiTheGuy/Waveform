package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.SettingType;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.ui.drawers.DotPlotDrawer;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class PhasePlotDrawer extends DotPlotDrawer {
    public static final double DEFAULT_PHASE_FACTOR = 0.2;
    private double phaseFactor;
    private int phaseFrames;

    public PhasePlotDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    
    @Override
    public BufferedImage drawFullAudio() {
        short[] monoData = playingAudio.getMonoData();
        int[] mappedCurrentData = mapArrayToPixelCoords(monoData, Waveform.WIDTH);
        int[] mappedPhaseData = mapArrayToPixelCoords(monoData, Waveform.HEIGHT);
        BufferedImage image = createBlankImage();
        for (int i = phaseFrames; i < mappedCurrentData.length; i++) {
            int x = Math.min(Math.abs(mappedCurrentData[i]), Waveform.WIDTH - 1);
            int y = Math.min(Math.abs(mappedPhaseData[i]), Waveform.HEIGHT - 1);
            image.setRGB(x, y, Config.foregroundColor.getRGB());
        }
        return image;
    }

    @Override
    public BufferedImage drawFrame(double sec) {
        short[] monoData = playingAudio.getMonoData();
        if (sec < phaseFactor) return createBlankImage();
        int samplesThisFrame = (int) Math.min(Config.getFrameLength() * playingAudio.sampleRate(), (playingAudio.duration() - sec) * playingAudio.sampleRate());
        int currentIndex = (int) (sec * playingAudio.sampleRate());
        int phaseIndex = Math.max(0, currentIndex - phaseFrames);
        short[] currentData = Arrays.copyOfRange(monoData, currentIndex, currentIndex + samplesThisFrame);
        short[] phaseData = Arrays.copyOfRange(monoData, phaseIndex, phaseIndex + samplesThisFrame);
        return drawData(currentData, phaseData);
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        updatePhaseFactor();
    }

    @Override
    public void regenerateIfNeeded() {
        updatePhaseFactor();
    }

    private void updatePhaseFactor() {
        phaseFactor = getSetting("phase_factor", Double.class);
        phaseFrames = (int) (phaseFactor * playingAudio.sampleRate());
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("phase_factor", SettingType.positiveDouble(), DEFAULT_PHASE_FACTOR);
    }
}
