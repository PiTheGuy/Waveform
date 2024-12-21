package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.main.Visualizer;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.config.visualizersettings.options.ColorChannel;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class HarmonicPercussiveDecompDrawer extends HeatmapDrawer {
    public HarmonicPercussiveDecompDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected BufferedImage precomputeImage() {
        double[][] spectrogramData = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), context.getWidth());
        double[][] harmonicSpectrogramData = applyHorizontalMedianFilter(spectrogramData);
        double[][] percussiveSpectrogramData = applyVerticalMedianFilter(spectrogramData);
        double[][] harmonicSignal = FftAnalyser.batchInverseFFT(harmonicSpectrogramData);
        double[][] percussiveSignal = FftAnalyser.batchInverseFFT(percussiveSpectrogramData);
        Signals signals = normalize(harmonicSignal, percussiveSignal);
        BufferedImage image = createBlankImage();
        for (int x = 0; x < context.getWidth(); x++) {
            for (int y = 0; y < context.getHeight(); y++) {
                int scaleFactor = Config.highContrast ? 6 : 4;
                float harmonicIntensity = (float) Math.min(signals.harmonic[x][y] * scaleFactor, 1);
                float percussiveIntensity = (float) Math.min(signals.percussive[x][y] * scaleFactor, 1);
                Color color = getColor(harmonicIntensity, percussiveIntensity);
                image.setRGB(x, context.getHeight() - 1 - y, color.getRGB());
            }
        }
        return image;
    }

    private Signals normalize(double[][] harmonic, double[][] percussive) {
        double max = Arrays.stream(harmonic).flatMapToDouble(Arrays::stream).max().orElseThrow();
        max = Math.max(max, Arrays.stream(percussive).flatMapToDouble(Arrays::stream).max().orElseThrow());
        double[][] newHarmonic = new double[context.getWidth()][context.getHeight()];
        double[][] newPercussive = new double[context.getWidth()][context.getHeight()];
        for (int x = 0; x < context.getWidth(); x++) {
            for (int y = 0; y < context.getHeight(); y++) {
                newHarmonic[x][y] = Math.abs(harmonic[x][y] / max);
                newPercussive[x][y] = Math.abs(percussive[x][y] / max);
            }
        }
        return new Signals(newHarmonic, newPercussive);
    }

    private static double[][] applyHorizontalMedianFilter(double[][] data) {
        double[][] filteredData = new double[data.length][data[0].length];
        int timeWindow = 5;
        int window = (timeWindow - 1) / 2;
        for (int freq = 0; freq < data.length; freq++) {
            double[] columnData = data[freq];
            for (int time = 0; time < data[0].length; time++) {
                int startIndex = Math.max(0, time - window);
                int endIndex = Math.min(columnData.length - 1, time + window);
                double[] medianData = Arrays.copyOfRange(columnData, startIndex, endIndex + 1);
                filteredData[freq][time] = median(medianData);
            }
        }
        return filteredData;
    }

    private double[][] applyVerticalMedianFilter(double[][] data) {
        double[][] transposed = Util.transpose(data);
        double[][] filteredData = applyHorizontalMedianFilter(transposed);
        return Util.transpose(filteredData);
    }

    private static double median(double[] values) {
        Arrays.sort(values);
        return values[values.length / 2];
    }

    private Color getColor(double harmonic, double percussive) {
        ColorChannel harmonicChannel = getSetting("harmonic", ColorChannel.class);
        ColorChannel percussiveChannel = getSetting("percussive", ColorChannel.class);
        int h = harmonicChannel.shift(harmonic);
        int p = percussiveChannel.shift(percussive);
        return new Color(h | p);
    }

    @Override
    public Visualizer getVisualizer() {
        return Visualizer.HARMONIC_PERCUSSIVE_DECOMPOSITION;
    }

    @Override
    public SettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("harmonic", SettingType.forEnum(ColorChannel.class), ColorChannel.RED)
                .addSetting("percussive", SettingType.forEnum(ColorChannel.class), ColorChannel.GREEN);
    }

    private record Signals(double[][] harmonic, double[][] percussive) {
    }
}
