package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.config.visualizersettings.options.ColorChannel;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class TransientSustainedDrawer extends HeatmapDrawer {
    public TransientSustainedDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected BufferedImage precomputeImage() {
        double[][] frequencyData = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), context.getWidth() + 1);
        double[][] diffData = new double[frequencyData.length - 1][frequencyData[0].length];
        for (int time = 1; time < frequencyData.length; time++)
            for (int band = 0; band < frequencyData[0].length; band++)
                diffData[time - 1][band] = Math.max(frequencyData[time][band] - frequencyData[time - 1][band], 0);
        double[][] transientData = computeTransientData(diffData, frequencyData);
        double[][] sustainedData = computeSustainedData(frequencyData, transientData);
        return drawSpectrogram(transientData, sustainedData);
    }

    private BufferedImage drawSpectrogram(double[][] transientData, double[][] sustainedData) {
        for (int x = 0; x < context.getWidth(); x++) {
            transientData[x] = FftAnalyser.resampleMagnitudesToBands(transientData[x], context.getWidth());
            sustainedData[x] = FftAnalyser.resampleMagnitudesToBands(sustainedData[x], context.getWidth());
        }
        if (getSetting("normalize", Boolean.class)) {
            int scaleFactor = Config.highContrast ? 15 : 10;
            return drawData(Util.normalize(transientData), Util.normalize(sustainedData), scaleFactor);
        } else return drawData(transientData, sustainedData, 1);
    }

    private BufferedImage drawData(double[][] transientData, double[][] sustainedData, double scaleFactor) {
        BufferedImage image = createBlankImage();
        for (int x = 0; x < context.getWidth(); x++) {
            for (int y = 0; y < context.getHeight(); y++) {
                float trans = (float) Math.min(transientData[x][y] * scaleFactor, 1);
                float sustained = (float) Math.min(sustainedData[x][y] * scaleFactor, 1);
                Color color = getColor(trans, sustained);
                image.setRGB(x, context.getHeight() - 1 - y, color.getRGB());
            }
        }
        return image;
    }

    private double[][] computeTransientData(double[][] diffData, double[][] frequencyData) {
        double[][] transientData = new double[diffData.length][diffData[0].length];
        double threshold = computeThreshold(diffData);
        for (int time = 0; time < frequencyData.length - 1; time++)
            for (int band = 0; band < frequencyData[0].length; band++)
                transientData[time][band] = diffData[time][band] > threshold ? frequencyData[time][band] : 0;
        return transientData;
    }

    private double[][] computeSustainedData(double[][] frequencyData, double[][] transientData) {
        double[][] data = Arrays.copyOf(frequencyData, context.getWidth());
        return switch (getSetting("calculation_mode", CalculationMode.class)) {
            case LOW_PASS -> {
                int windowSize = getSetting("sustained_window_size", Integer.class);
                double[][] smoothed = new double[data.length][data[0].length];
                for (int band = 0; band < data[0].length; band++) {
                    for (int time = 0; time < data.length; time++) {
                        int start = Math.max(0, time - windowSize / 2);
                        int end = Math.min(data.length - 1, time + windowSize / 2);
                        double sum = 0;
                        for (int i = start; i <= end; i++) sum += data[i][band];
                        smoothed[time][band] = sum / (end - start + 1);
                    }
                }
                yield smoothed;
            }
            case SUBTRACT -> {
                double[][] result = new double[data.length][data[0].length];
                for (int time = 0; time < data.length; time++)
                    for (int band = 0; band < data[0].length; band++)
                        result[time][band] = data[time][band] - transientData[time][band];
                yield result;
            }
        };
    }

    private Color getColor(double trans, double sustained) {
        ColorChannel transientChannel = getSetting("transient_channel", ColorChannel.class);
        ColorChannel sustainedChannel = getSetting("sustained_channel", ColorChannel.class);
        int t = transientChannel.shift(trans);
        int s = sustainedChannel.shift(sustained);
        return new Color(t | s);
    }

    private double computeThreshold(double[][] data) {
        double[] allData = Arrays.stream(data).flatMapToDouble(Arrays::stream).toArray();
        double average = Arrays.stream(allData).average().orElseThrow();
        double stdDev = Math.sqrt(Arrays.stream(allData).map(value -> (value - average) * (value - average)).average().orElseThrow());
        return average + stdDev;
    }

    @Override
    public SettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("calculation_mode", SettingType.forEnum(CalculationMode.class), CalculationMode.LOW_PASS)
                .addSetting("transient_channel", SettingType.forEnum(ColorChannel.class), ColorChannel.RED)
                .addSetting("sustained_channel", SettingType.forEnum(ColorChannel.class), ColorChannel.GREEN)
                .addSetting("sustained_window_size", SettingType.positiveInt(), 5)
                .addSetting("normalize", SettingType.BOOLEAN, true);
    }

    public enum CalculationMode {
        LOW_PASS("Low Pass"),
        SUBTRACT("Subtract");

        private final String name;

        CalculationMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
