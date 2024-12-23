package pitheguy.waveform.ui.drawers.waveform;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.drawers.MappedPlotDrawer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class WaveformDrawer extends MappedPlotDrawer {
    public WaveformDrawer(DrawContext context) {
        super(context);
    }

    public static BufferedImage drawData(DrawContext context, BufferedImage image, int[] top, int[] bottom) {
        Graphics2D g = image.createGraphics();
        g.setColor(Config.foregroundColor());
        int halfHeight = context.getHeight() / 2;
        double scale = (double) top.length / context.getWidth();
        for (int x = 0; x < context.getWidth(); x++) {
            int i = (int) (x * scale);
            int y1 = (int) (halfHeight - (double) top[i] / 2);
            int y2 = (int) (halfHeight + (double) bottom[i] / 2);
            g.drawLine(x, y1, x, y2);
        }
        g.dispose();
        return image;
    }

    protected int[] mapArrayToPixelHeight(short[] input) {
        int[] output = new int[input.length];
        for (int i = 0; i < input.length; i++)
            output[i] = (int) Math.abs(((double) input[i] / maxValue * context.getHeight()));
        return output;
    }

    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        super.drawAudio(sec, length);
        if (left == null || right == null) return createBlankImage();
        int[] leftMapped = mapArrayToPixelHeight(left);
        int[] rightMapped = mapArrayToPixelHeight(right);
        return drawData(context, createBlankImage(), leftMapped, rightMapped);
    }

    @Override
    public boolean isSeekingAllowed() {
        return Config.playerMode || Config.showProgress;
    }
}
