package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class OnsetEnvelopeDrawer extends SlicedImageDrawer {

    public OnsetEnvelopeDrawer(DrawContext context) {
        super(context);
    }

    
    @Override
    public BufferedImage precomputeImage() {
        short[] monoData = playingAudio.getMonoData();
        double[][] frequencyData = FftAnalyser.getFrequencyData(monoData, getImageWidth());
        return drawOnsetEnvelope(frequencyData, createBlankImage());
    }

    private BufferedImage drawOnsetEnvelope(double[][] frequencyData, BufferedImage image) {
        double[] energy = Arrays.stream(frequencyData).mapToDouble(frequencies -> Arrays.stream(frequencies).sum()).toArray();
        double maxValue = Arrays.stream(energy).max().orElseThrow();
        int[] pixelHeights = mapArrayToPixelHeight(energy, maxValue);
        Graphics2D g = image.createGraphics();
        g.setColor(Config.foregroundColor);
        for (int x = 0; x < pixelHeights.length; x++) g.drawLine(x, getImageHeight(context) - 1, x, getImageHeight(context) - 1 - pixelHeights[x]);
        return image;
    }

    public int[] mapArrayToPixelHeight(double[] data, double maxValue) {
        int[] pixelHeights = new int[data.length];
        for (int i = 0; i < data.length; i++) pixelHeights[i] = (int) (data[i] / maxValue * getImageHeight(context));
        return pixelHeights;
    }

    @Override
    public boolean isSeekingAllowed() {
        return Config.playerMode;
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
    }
}
