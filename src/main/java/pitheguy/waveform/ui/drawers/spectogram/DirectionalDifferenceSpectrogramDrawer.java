package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class DirectionalDifferenceSpectrogramDrawer extends HeatmapDrawer {
    public DirectionalDifferenceSpectrogramDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    
    @Override
    protected BufferedImage precomputeImage() {
        double[][] leftMagnitudes = FftAnalyser.getFrequencyData(playingAudio.left(), Waveform.WIDTH);
        double[][] rightMagnitudes = FftAnalyser.getFrequencyData(playingAudio.right(), Waveform.WIDTH);
        double[][] difference = new double[leftMagnitudes.length][leftMagnitudes[0].length];
        for (int i = 0; i < leftMagnitudes.length; i++)
            for (int j = 0; j < leftMagnitudes[0].length; j++)
                difference[i][j] = leftMagnitudes[i][j] - rightMagnitudes[i][j];
        double[][] displayData = Arrays.stream(difference)
                .map(data -> FftAnalyser.resampleMagnitudesToBands(data, Waveform.HEIGHT))
                .toArray(double[][]::new);
        return drawData(displayData);
    }
}
