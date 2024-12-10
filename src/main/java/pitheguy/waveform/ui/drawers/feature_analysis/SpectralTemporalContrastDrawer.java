package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.SettingType;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class SpectralTemporalContrastDrawer extends HeatmapDrawer {
    public SpectralTemporalContrastDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected BufferedImage precomputeImage() {
        double[][] frequencyData = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), getImageWidth() + 1);
        double[][] spectrogram = new double[getImageWidth() + 1][];
        Arrays.setAll(spectrogram, i -> FftAnalyser.resampleMagnitudesToBands(frequencyData[i], getImageHeight(context) + 1));
        double[][] temporalContrast = new double[getImageWidth()][getImageHeight(context)];
        for (int time = 1; time < spectrogram.length; time++)
            for (int band = 0; band < spectrogram[time].length - 1; band++)
                temporalContrast[time - 1][band] = Math.abs(spectrogram[time][band] - spectrogram[time - 1][band]);
        double[][] spectralContrast = new double[getImageWidth()][getImageHeight(context)];
        for (int time = 0; time < spectrogram.length - 1; time++)
            for (int band = 1; band < spectrogram[time].length; band++)
                spectralContrast[time][band - 1] = Math.abs(spectrogram[time][band] - spectrogram[time][band - 1]);
        double[][] contrast = new double[getImageWidth()][getImageHeight(context)];
        for (int time = 0; time < contrast.length - 1; time++)
            for (int band = 0; band < contrast[time].length; band++)
                contrast[time][band] = geomMean(temporalContrast[time][band], spectralContrast[time][band]);
        if (getSetting("normalize", Boolean.class)) {
            contrast = Util.normalize(contrast);
            double scaleFactor = Config.highContrast ? 15 : 10;
            Arrays.stream(contrast).forEach(arr -> Arrays.setAll(arr, i -> arr[i] * scaleFactor));
        }
        return drawData(context, contrast);
    }

    private static double geomMean(double v1, double v2) {
        return Math.sqrt(v1 * v1 + v2 * v2);
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("normalize", SettingType.bool(), false);
    }
}
