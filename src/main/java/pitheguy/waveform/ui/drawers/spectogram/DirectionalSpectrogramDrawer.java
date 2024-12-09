package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class DirectionalSpectrogramDrawer extends HeatmapDrawer {
    public DirectionalSpectrogramDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    
    @Override
    protected BufferedImage precomputeImage() {
        double[][] leftMagnitudes = FftAnalyser.getFrequencyData(playingAudio.left(), Waveform.WIDTH);
        double[][] rightMagnitudes = FftAnalyser.getFrequencyData(playingAudio.right(), Waveform.WIDTH);
        double[][] leftDisplayData = Arrays.stream(leftMagnitudes)
                .map(data -> FftAnalyser.resampleMagnitudesToBands(data, Waveform.HEIGHT))
                .toArray(double[][]::new);
        double[][] rightDisplayData = Arrays.stream(rightMagnitudes)
                .map(data -> FftAnalyser.resampleMagnitudesToBands(data, Waveform.HEIGHT))
                .toArray(double[][]::new);
        BufferedImage image = createBlankImage();
        for (int x = 0; x < Waveform.WIDTH; x++) {
            for (int y = 0; y < Waveform.HEIGHT; y++) {
                double left = Math.min(leftDisplayData[x][y], 1);
                double right = Math.min(rightDisplayData[x][y], 1);
                Color color = new Color((float) left, (float) right, 0);
                image.setRGB(x, Waveform.HEIGHT - 1 - y, color.getRGB());
            }
        }
        return image;
    }
}
