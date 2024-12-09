package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.drawers.RangeLineGraphDrawer;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class SpectralDistributionDrawer extends RangeLineGraphDrawer {

    public SpectralDistributionDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    
    @Override
    protected BufferedImage precomputeImage() {
        short[] monoData = playingAudio.getMonoData();
        double[][] frequencyData = FftAnalyser.getFrequencyData(monoData, Waveform.WIDTH);
        double[] averages = new double[Waveform.WIDTH];
        double[] deviations = new double[Waveform.WIDTH];
        for (int x = 0; x < Waveform.WIDTH; x++) {
            double[] magnitudes = frequencyData[x];
            double average = calculateWeightedAverage(magnitudes);
            averages[x] = normalizeFrequency(average);
            deviations[x] = normalizeFrequency(calculateStandardDeviation(magnitudes, average));
        }
        return drawData(averages, deviations);
    }

    private double normalizeFrequency(double frequency) {
        return frequency / playingAudio.sampleRate();
    }

    private double calculateWeightedAverage(double[] magnitudes) {
        double frequencyResolution = playingAudio.sampleRate() / magnitudes.length;
        double totalWeight = Arrays.stream(magnitudes).sum();
        double weightedSum = 0;
        for (int i = 0; i < magnitudes.length; i++) {
            double frequency = i * frequencyResolution;
            weightedSum += frequency * magnitudes[i];
        }
        return totalWeight == 0 ? 0 : weightedSum / totalWeight;
    }

    private double calculateStandardDeviation(double[] magnitudes, double average) {
        double frequencyResolution = playingAudio.sampleRate() / magnitudes.length;
        double totalWeight = Arrays.stream(magnitudes).sum();
        double deviationSum = 0;
        for (int i = 0; i < magnitudes.length; i++) {
            double frequency = i * frequencyResolution;
            deviationSum += Math.pow(frequency - average, 2) * magnitudes[i];
        }
        return Math.sqrt(deviationSum / totalWeight);
    }
}
