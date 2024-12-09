package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.ui.drawers.LoudnessDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class LoudnessSpectrogramDrawer extends HeatmapDrawer {
    public LoudnessSpectrogramDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    @Override
    protected BufferedImage precomputeImage() {
        double[][] frequencyData = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), Waveform.WIDTH);
        double[][] spectrogramData = new double[Waveform.WIDTH][];
        for (int i = 0; i < frequencyData.length; i++) {
            double[] scaledFrequencyData = new double[frequencyData[i].length];
            double frequencyResolution = playingAudio.sampleRate() / frequencyData[i].length;
            for (int j = 0; j < frequencyData[i].length; j++) {
                if (j == 0) {
                    scaledFrequencyData[j] = 0;
                    continue;
                }
                double frequency = j * frequencyResolution;
                double magnitude = frequencyData[i][j];
                scaledFrequencyData[j] = magnitude * LoudnessDrawer.frequencyMagnitude(frequency);
            }
            spectrogramData[i] = FftAnalyser.resampleMagnitudesToBands(scaledFrequencyData, Waveform.HEIGHT);
        }
        double[][] displayData = Util.normalize(spectrogramData);
        int scaleFactor = Config.highContrast ? 20 : 10;
        Arrays.setAll(displayData, i -> Arrays.stream(displayData[i]).map(v -> v * scaleFactor).toArray());
        return drawData(displayData);
    }


}