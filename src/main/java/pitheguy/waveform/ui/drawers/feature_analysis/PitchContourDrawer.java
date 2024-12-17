package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.drawers.BarGraphDrawer;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class PitchContourDrawer extends HeatmapDrawer {
    public PitchContourDrawer(DrawContext context) {
        super(context);
    }

    
    @Override
    protected BufferedImage precomputeImage() {
        BufferedImage image = createBlankImage();
        short[] monoData = playingAudio.getMonoData();
        double[] pitchContour = new double[context.getWidth()];
        double[] pitchSalience = new double[context.getWidth()];
        double[][] frequencyData = FftAnalyser.getFrequencyData(monoData, context.getWidth());
        for (int x = 0; x < context.getWidth(); x++) {
            Result result = calculatePitch(frequencyData[x]);
            pitchContour[x] = result.pitch;
            pitchSalience[x] = result.salience;
        }
        pitchContour = Util.normalize(pitchContour);
        pitchSalience = Util.normalize(pitchSalience);
        Graphics2D g = image.createGraphics();
        int[] pixelHeights = BarGraphDrawer.mapArrayToPixelHeight(context, pitchContour);
        for (int x = 0; x < pixelHeights.length; x++) {
            g.setColor(getSecondaryColor(pitchSalience[x]));
            g.drawLine(x, context.getHeight() - 1, x, context.getHeight() - 1 - pixelHeights[x]);
        }
        return image;
    }

    private Result calculatePitch(double[] spectrum) {
        int maxDownsampleFactor = 5;
        int minLength = spectrum.length / maxDownsampleFactor;
        double[][] downsampledSpectrums = new double[maxDownsampleFactor - 1][minLength];
        for (int factor = 2; factor <= maxDownsampleFactor; factor++)
            downsampledSpectrums[factor - 2] = downsample(spectrum, factor, minLength);
        double[] resultSpectrum = new double[minLength];
        for (int i = 0; i < minLength; i++) {
            double value = 1;
            for (double[] downsampledSpectrum : downsampledSpectrums) value *= downsampledSpectrum[i];
            resultSpectrum[i] = value;
        }
        int fundamentalIndex = 0;
        double maxValue = resultSpectrum[0];
        for (int i = 1; i < resultSpectrum.length; i++) {
            if (resultSpectrum[i] > maxValue) {
                maxValue = resultSpectrum[i];
                fundamentalIndex = i;
            }
        }
        double frequencyResolution = playingAudio.sampleRate() / spectrum.length;
        double pitch = fundamentalIndex * frequencyResolution;
        double salience = Arrays.stream(resultSpectrum).max().getAsDouble();
        return new Result(pitch, salience);
    }

    private double[] downsample(double[] data, int factor, int minLength) {
        double[] downsampled = new double[minLength];
        for (int i = 0; i < minLength; i++) downsampled[i] = data[i * factor];
        return downsampled;
    }

    private record Result(double pitch, double salience) {
    }
}
