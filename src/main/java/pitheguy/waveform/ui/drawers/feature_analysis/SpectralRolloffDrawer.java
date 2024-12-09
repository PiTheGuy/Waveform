package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.*;
import pitheguy.waveform.ui.drawers.LineGraphDrawer;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class SpectralRolloffDrawer extends LineGraphDrawer {
    private static final double DEFAULT_CUTOFF = 0.8;

    
    public SpectralRolloffDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    @Override
    protected BufferedImage precomputeImage() {
        short[] monoData = playingAudio.getMonoData();
        double[][] frequencyData = FftAnalyser.getFrequencyData(monoData, Waveform.WIDTH);
        double[] cutoffFrequencies = Arrays.stream(frequencyData).mapToDouble(this::getCutoffFrequency).toArray();
        double[] displayData = Arrays.stream(cutoffFrequencies).map(value -> value / playingAudio.sampleRate()).toArray();
        return drawData(displayData);
    }

    private double getCutoffFrequency(double[] magnitudes) {
        double cutoff = getSetting("cutoff", Double.class);
        double frequencyResolution = playingAudio.sampleRate() / magnitudes.length;
        double totalEnergy = Arrays.stream(magnitudes).sum();
        int cutoffBin = 0;
        double energyEncountered = 0;
        while (energyEncountered / totalEnergy < cutoff && cutoffBin < magnitudes.length) energyEncountered += magnitudes[cutoffBin++];
        if (cutoffBin == magnitudes.length && energyEncountered / totalEnergy < cutoff) return 0;
        return cutoffBin * frequencyResolution;
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("cutoff", SettingType.fraction(), DEFAULT_CUTOFF);
    }
}