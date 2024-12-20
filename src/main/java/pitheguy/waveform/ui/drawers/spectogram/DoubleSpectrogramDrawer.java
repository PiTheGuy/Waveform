package pitheguy.waveform.ui.drawers.spectogram;

import org.apache.commons.math3.complex.Complex;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.config.visualizersettings.options.ColorChannel;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DoubleSpectrogramDrawer extends AbstractSpectrogramDrawer {
    public DoubleSpectrogramDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected BufferedImage precomputeImage() {
        Complex[][] fftData = FftAnalyser.getComplexFrequencyData(playingAudio.getMonoData(), context.getWidth());
        double[][] magnitudes = new double[fftData.length][];
        double[][] phases = new double[fftData.length][];
        for (int i = 0; i < fftData.length; i++) {
            magnitudes[i] = new double[fftData[i].length];
            phases[i] = new double[fftData[i].length];
            for (int j = 0; j < fftData[i].length; j++) {
                magnitudes[i][j] = getMagnitude(fftData[i][j].getReal(), fftData[i][j].getImaginary());
                phases[i][j] = PhaseSpectrogramDrawer.getPhase(fftData[i][j].getReal(), fftData[i][j].getImaginary());
            }
            magnitudes[i] = resample(magnitudes[i]);
            phases[i] = resample(phases[i]);
        }
        phases = Util.normalize(phases);
        BufferedImage spectrogram = createBlankImage();
        for (int x = 0; x < context.getWidth(); x++) {
            for (int y = 0; y < context.getHeight(); y++) {
                float magnitude = (float) Math.min(magnitudes[x][y], 1);
                float phase = (float) Math.min(phases[x][y], 1);
                Color color = getColor(magnitude, phase);
                spectrogram.setRGB(x, context.getHeight() - 1 - y, color.getRGB());
            }
        }
        return spectrogram;
    }

    private static double getMagnitude(double real, double imag) {
        return Math.sqrt(real * real + imag * imag);
    }

    private Color getColor(double magnitude, double phase) {
        ColorChannel magnitudeChannel = getSetting("magnitude", ColorChannel.class);
        ColorChannel phaseChannel = getSetting("phase", ColorChannel.class);
        int mag = magnitudeChannel.shift(magnitude);
        int ph = phaseChannel.shift(phase);
        return new Color(mag | ph);
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("magnitude", SettingType.forEnum(ColorChannel.class), ColorChannel.RED)
                .addSetting("phase", SettingType.forEnum(ColorChannel.class), ColorChannel.GREEN);
    }
}
