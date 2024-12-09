package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class AbstractSpectrogramDrawer extends HeatmapDrawer {
    public AbstractSpectrogramDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    @Override
    protected BufferedImage precomputeImage() {
        BufferedImage spectrogram = createBlankImage();
        double[][] spectrogramData = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), Waveform.WIDTH);
        for (int x = 0; x < Waveform.WIDTH; x++) {
            double[] data = resample(spectrogramData[x]);
            for (int y = 0; y < Waveform.HEIGHT; y++) {
                Color color = getColor(data[y]);
                spectrogram.setRGB(x, Waveform.HEIGHT - 1 - y, color.getRGB());
            }
        }
        return spectrogram;
    }

    protected double[] resample(double[] magnitudes) {
        return FftAnalyser.resampleMagnitudesToBands(magnitudes, Waveform.HEIGHT);
    }
}
