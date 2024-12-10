package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.drawers.RangeLineGraphDrawer;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.image.BufferedImage;

public class SpectralCentroidDrawer extends RangeLineGraphDrawer {
    public SpectralCentroidDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected BufferedImage precomputeImage() {
        short[] monoData = playingAudio.getMonoData();
        double[][] frequencyData = FftAnalyser.getFrequencyData(monoData, getImageWidth());
        SpectralData spectralData = computeSpectralData(frequencyData);
        return drawData(spectralData.centroids(), spectralData.bandwidths());
    }

    protected SpectralData computeSpectralData(double[][] frequencyData) {
        double[] spectralCentroids = new double[frequencyData.length];
        double[] spectralBandwidths = new double[frequencyData.length];
        for (int i = 0; i < frequencyData.length; i++) {
            double centroid = calculateSpectralCentroid(frequencyData[i]);
            spectralCentroids[i] = normalizeFrequency(centroid);
            spectralBandwidths[i] = normalizeFrequency(calculateSpectralBandwidth(frequencyData[i], centroid));
        }
        return new SpectralData(spectralCentroids, spectralBandwidths);
    }

    private double normalizeFrequency(double frequency) {
        return frequency / playingAudio.sampleRate();
    }

    private double calculateSpectralCentroid(double[] magnitudes) {
        double frequencyResolution = playingAudio.sampleRate() / magnitudes.length;
        double numerator = 0, denominator = 0;
        for (int i = 0; i < magnitudes.length; i++) {
            double frequency = frequencyResolution * i;
            double magnitude = magnitudes[i];
            numerator += magnitude * frequency;
            denominator += magnitude;
        }
        return numerator / denominator;
    }

    private double calculateSpectralBandwidth(double[] magnitudes, double centroid) {
        double frequencyResolution = playingAudio.sampleRate() / magnitudes.length;
        double numerator = 0, denominator = 0;
        for (int i = 0; i < magnitudes.length; i++) {
            double frequency = frequencyResolution * i;
            double magnitude = magnitudes[i];
            numerator += Math.pow(frequency - centroid, 2) * magnitude;
            denominator += magnitude;
        }
        return Math.sqrt(numerator / denominator);
    }

    protected record SpectralData(double[] centroids, double[] bandwidths) {
    }
}
