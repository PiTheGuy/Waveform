package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.drawers.LineGraphDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class SpectralCrestDrawer extends LineGraphDrawer {
    public SpectralCrestDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    @Override
    protected BufferedImage precomputeImage() {
        double[][] frequencyData = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), Waveform.WIDTH);
        double[] displayData = new double[frequencyData.length];
        for (int time = 0; time < frequencyData.length; time++) {
            double[] magnitudes = frequencyData[time];
            double max = Arrays.stream(magnitudes).max().orElseThrow();
            double average = Arrays.stream(magnitudes).average().orElseThrow();
            displayData[time] = max / average;
        }
        return drawData(Util.normalize(displayData));
    }
}
