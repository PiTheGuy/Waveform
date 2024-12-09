package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.drawers.LineGraphDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class SpectralFlatnessDrawer extends LineGraphDrawer {

    public SpectralFlatnessDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    @Override
    protected BufferedImage precomputeImage() {
        double[][] frequencyData = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), Waveform.WIDTH);
        double[] flatnessData = Arrays.stream(frequencyData).mapToDouble(SpectralFlatnessDrawer::calculateFlatness).toArray();
        return drawData(Util.normalize(flatnessData));
    }

    private static double calculateFlatness(double[] magnitudes) {
        double geometricMean = geometricMean(magnitudes);
        double arithmeticMean = arithmeticMean(magnitudes);
        return arithmeticMean == 0.0 ? 0.0 : geometricMean / arithmeticMean;
    }

    private static double geometricMean(double[] data) {
        double logSum = Arrays.stream(data).map(Math::log).sum();
        return Math.exp(logSum / data.length);
    }

    private static double arithmeticMean(double[] data) {
        return Arrays.stream(data).average().orElse(0.0);
    }
}
