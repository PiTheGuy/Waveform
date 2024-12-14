package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class DirectionalSpectrogramDrawer extends HeatmapDrawer {
    public DirectionalSpectrogramDrawer(DrawContext context) {
        super(context);
    }

    
    @Override
    protected BufferedImage precomputeImage() {
        double[][] leftMagnitudes = FftAnalyser.getFrequencyData(playingAudio.left(), context.getWidth());
        double[][] rightMagnitudes = FftAnalyser.getFrequencyData(playingAudio.right(), context.getWidth());
        double[][] leftDisplayData = Arrays.stream(leftMagnitudes)
                .map(data -> {
                    return FftAnalyser.resampleMagnitudesToBands(data, context.getHeight());
                })
                .toArray(double[][]::new);
        double[][] rightDisplayData = Arrays.stream(rightMagnitudes)
                .map(data -> {
                    return FftAnalyser.resampleMagnitudesToBands(data, context.getHeight());
                })
                .toArray(double[][]::new);
        BufferedImage image = createBlankImage();
        for (int x = 0; x < context.getWidth(); x++) {
            for (int y = 0; y < context.getHeight(); y++) {
                double left = Math.min(leftDisplayData[x][y], 1);
                double right = Math.min(rightDisplayData[x][y], 1);
                Color color = new Color((float) left, (float) right, 0);
                image.setRGB(x, context.getHeight() - 1 - y, color.getRGB());
            }
        }
        return image;
    }
}
