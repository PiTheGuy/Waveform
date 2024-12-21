package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.ui.drawers.LineGraphDrawer;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class SpectralRolloffDrawer extends LineGraphDrawer {
    private static final double DEFAULT_CUTOFF = 0.8;

    
    public SpectralRolloffDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected BufferedImage precomputeImage() {
        short[] monoData = playingAudio.getMonoData();
        double[][] frequencyData = FftAnalyser.getFrequencyData(monoData, context.getWidth());
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
    public SettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("cutoff", SettingType.fraction(), DEFAULT_CUTOFF);
    }
}
