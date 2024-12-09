package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.SettingType;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.options.VisualizationMode;
import pitheguy.waveform.ui.util.DebugText;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class LoudnessDrawer extends CompoundDrawer {
    public LoudnessDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    @Override
    protected AudioDrawer getDrawer() {
        VisualizationMode mode = getSetting("visualization_mode", VisualizationMode.class);
        return switch (mode) {
            case INSTANTANEOUS -> new Instantaneous(forceFullAudio);
            case GRAPH -> new Graph(forceFullAudio);
        };
    }

    private static class Instantaneous extends SmoothedAudioDrawer {
        public Instantaneous(boolean forceFullAudio) {
            super(forceFullAudio, 10, false);
        }

        @Override
        protected BufferedImage drawAudio(double sec, double length) {
            updateAudioData(sec, length);
            double[] monoData = Util.normalize(AudioData.averageChannels(left, right));
            double[] frequencyData = FftAnalyser.performFFT(monoData);
            double loudness = calculateLoudness(frequencyData, playingAudio.sampleRate());
            BufferedImage image = createBlankImage();
            Graphics2D g = image.createGraphics();
            g.setColor(Config.foregroundColor);
            double displayValue = Math.min(getDisplayValue(loudness / 3), 1);
            int height = (int) (displayValue * Waveform.HEIGHT);
            g.fillRect(0, Waveform.HEIGHT - height, image.getWidth(), height);
            drawDebugText(g, new DebugText().add("Loudness", loudness).add("Displayed", displayValue), Color.RED);
            return image;
        }
    }

    private static class Graph extends LineGraphDrawer {
        public Graph(boolean forceFullAudio) {
            super(forceFullAudio);
        }

        @Override
        protected BufferedImage precomputeImage() {
            double[][] frequencyData = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), Waveform.WIDTH);
            double[] loudnessData = new double[Waveform.WIDTH];
            for (int x = 0; x < frequencyData.length; x++)
                loudnessData[x] = LoudnessDrawer.calculateLoudness(frequencyData[x], playingAudio.sampleRate());
            return drawData(Util.normalize(loudnessData));
        }
    }

    public static double calculateLoudness(double[] frequencyData, double frameRate) {
        double[] scaledFrequencyData = new double[frequencyData.length];
        double frequencyResolution = frameRate / frequencyData.length;
        for (int i = 0; i < frequencyData.length; i++) {
            if (i == 0) {
                scaledFrequencyData[i] = 0;
                continue;
            }
            double frequency = i * frequencyResolution;
            scaledFrequencyData[i] = frequencyData[i] * LoudnessDrawer.frequencyMagnitude(frequency);
        }
        double averagePower = Arrays.stream(scaledFrequencyData).map(v -> v * v).average().orElseThrow();
        return Math.log10(averagePower + 1e-9); // Avoid log(0)
    }

    public static double frequencyMagnitude(double frequency) {
        return rlb(frequency) * highPassFilter(frequency) * shelvingFilter(frequency);
    }

    private static double rlb(double frequency) {
        return transferFunction(frequency, 38);
    }

    private static double highPassFilter(double frequency) {
        return transferFunction(frequency, 100);
    }

    private static double shelvingFilter(double frequency) {
        double s = Math.PI * 2 * frequency;
        double w = Math.PI * 2 * 1000;
        double numerator = s * s + Math.sqrt(2) * w * s + w * w;
        double denominator = s * s + w / Math.sqrt(2) * s + w * w;
        return numerator / denominator;
    }

    private static double transferFunction(double frequency, double cutoff) {
        double s = Math.PI * 2 * frequency;
        double wc = Math.PI * 2 * cutoff;
        return s / Math.sqrt(s * s + wc * wc);
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("visualization_mode", SettingType.forEnum(VisualizationMode.class), VisualizationMode.INSTANTANEOUS);
    }


}
