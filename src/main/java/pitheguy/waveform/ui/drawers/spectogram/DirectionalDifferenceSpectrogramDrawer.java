package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class DirectionalDifferenceSpectrogramDrawer extends HeatmapDrawer {
    public DirectionalDifferenceSpectrogramDrawer(DrawContext context) {
        super(context);
    }

    
    @Override
    protected BufferedImage precomputeImage() {
        double[][] leftMagnitudes = FftAnalyser.getFrequencyData(playingAudio.left(), getImageWidth());
        double[][] rightMagnitudes = FftAnalyser.getFrequencyData(playingAudio.right(), getImageWidth());
        double[][] difference = new double[leftMagnitudes.length][leftMagnitudes[0].length];
        for (int i = 0; i < leftMagnitudes.length; i++)
            for (int j = 0; j < leftMagnitudes[0].length; j++)
                difference[i][j] = leftMagnitudes[i][j] - rightMagnitudes[i][j];
        double[][] displayData = Arrays.stream(difference)
                .map(data -> FftAnalyser.resampleMagnitudesToBands(data, getImageHeight(context)))
                .toArray(double[][]::new);
        return drawData(context, displayData);
    }
}
