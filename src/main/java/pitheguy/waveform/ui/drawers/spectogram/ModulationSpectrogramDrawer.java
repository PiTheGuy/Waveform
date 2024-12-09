package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ModulationSpectrogramDrawer extends AbstractSpectrogramDrawer {

    public ModulationSpectrogramDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    @Override
    protected BufferedImage precomputeImage() {
        double[][] spectrogramData = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), Waveform.WIDTH * 2);
        Arrays.stream(spectrogramData).forEach(array -> Arrays.setAll(array, i -> array[i] * array[i]));
        double[][] transposedData = Util.transpose(spectrogramData);
        double[][] modulationData = Util.transpose(FftAnalyser.batchFFT(transposedData));
        double[][] filteredData = applyFilters(modulationData);
        double[][] resampledData = new double[Waveform.WIDTH][];
        for (int i = 0; i < Waveform.WIDTH; i++) resampledData[i] = resample(filteredData[i]);
        return HeatmapDrawer.drawData(Util.normalize(resampledData));
    }

    private double[][] applyFilters(double[][] data) {
        Arrays.fill(data[0], 0);
        int cutoff = 3;
        for (int x = 0; x < data.length; x++) for (int y = 0; y < cutoff; y++) data[x][y] = 0;
        double[][] normalized = Util.normalize(data);
        for (int x = 0; x < data.length; x++) for (int y = 0; y < data[0].length; y++) normalized[x][y] = Math.log1p(data[x][y]);
        return normalized;
    }

}
