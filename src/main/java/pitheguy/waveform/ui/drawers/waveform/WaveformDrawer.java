package pitheguy.waveform.ui.drawers.waveform;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.drawers.MappedPlotDrawer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class WaveformDrawer extends MappedPlotDrawer {
    protected int[] leftMapped;
    protected int[] rightMapped;

    public WaveformDrawer(DrawContext context) {
        super(context);
    }

    public static BufferedImage drawData(DrawContext context, BufferedImage image, int[] top, int[] bottom) {
        Graphics2D g = image.createGraphics();
        g.setColor(Config.foregroundColor);
        int width = getImageWidth(context); // Store width and height in case it changes
        int height = getImageHeight(context);
        int halfHeight = height / 2;
        double scale = (double) top.length / width;
        for (int x = 0; x < width; x++) {
            int i = (int) (x * scale);
            int y1 = (int) (halfHeight - (double) top[i] / 2);
            int y2 = (int) (halfHeight + (double) bottom[i] / 2);
            g.drawLine(x, y1, x, y2);
        }
        g.dispose();
        return image;
    }

    void mapArrayToPixelHeight(short[] input, int[] output) {
        for (int i = 0; i < input.length; i++)
            output[i] = (int) Math.abs(((double) input[i] / maxValue * getImageHeight(context)));
    }

    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        super.drawAudio(sec, length);
        mapArrayToPixelHeight(left, leftMapped);
        mapArrayToPixelHeight(right, rightMapped);
        return drawData(context, createBlankImage(), leftMapped, rightMapped);
    }

    protected void initializeDataArrays() {
        super.initializeDataArrays();
        double frameDuration = playerMode() ? playingAudio.duration() : Config.getFrameLength();
        int arraySize = (int) (frameDuration * playingAudio.sampleRate());
        leftMapped = new int[arraySize];
        rightMapped = new int[arraySize];
    }

    @Override
    public boolean isSeekingAllowed() {
        return Config.playerMode || Config.showProgress;
    }
}
