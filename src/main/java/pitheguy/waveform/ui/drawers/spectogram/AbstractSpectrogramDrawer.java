package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class AbstractSpectrogramDrawer extends HeatmapDrawer {
    public AbstractSpectrogramDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected BufferedImage precomputeImage() {
        BufferedImage spectrogram = createBlankImage();
        double[][] spectrogramData = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), getImageWidth());
        for (int x = 0; x < getImageWidth(); x++) {
            double[] data = resample(spectrogramData[x]);
            for (int y = 0; y < getImageHeight(context); y++) {
                Color color = getColor(data[y]);
                spectrogram.setRGB(x, getImageHeight(context) - 1 - y, color.getRGB());
            }
        }
        return spectrogram;
    }

    protected double[] resample(double[] magnitudes) {
        return FftAnalyser.resampleMagnitudesToBands(magnitudes, getImageHeight(context));
    }
}
