package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class CepstrumDrawer extends HeatmapDrawer {
    public CepstrumDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    
    @Override
    protected BufferedImage precomputeImage() {
        double[][] magnitudes = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), Waveform.WIDTH);
        double[][] scaledMagnitudes = Arrays.stream(magnitudes)
                .map(a -> Arrays.stream(a).map(v -> Math.log(v + 1e-9)).toArray())
                .toArray(double[][]::new);
        double[][] coefficients = FftAnalyser.batchInverseFFT(scaledMagnitudes);
        Arrays.stream(coefficients).forEach(data -> Arrays.setAll(data, i -> data[i] * 10));
        return drawData(coefficients);
    }
}