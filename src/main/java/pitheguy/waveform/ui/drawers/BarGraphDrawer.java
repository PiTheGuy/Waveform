package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.DrawContext;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class BarGraphDrawer extends AudioDrawer {
    private final boolean reverseDirection;

    public BarGraphDrawer(DrawContext context, boolean reverseDirection) {
        super(context);
        this.reverseDirection = reverseDirection;
    }

    protected BufferedImage drawArray(double[] data, BufferedImage image) {
        return drawArray(data, image, Config.foregroundColor);
    }


    protected BufferedImage drawArray(double[] data, BufferedImage image, Color color) {
        return drawArray(data, image, color, reverseDirection);
    }

    public BufferedImage drawArray(double[] data, BufferedImage image, Color color, boolean reverseDirection) {
        int[] pixelHeights = mapArrayToPixelHeight(context, data);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        double scale = (double) data.length / getImageWidth();
        for (int x = 0; x < getImageWidth(); x++) {
            int index = (int) (x * scale);
            int startY = reverseDirection ? 0 : getImageHeight(context) - 1;
            int endY = reverseDirection ? pixelHeights[index] : getImageHeight(context) - 1 - pixelHeights[index];
            g.drawLine(x, startY, x, endY);
        }
        g.dispose();
        return image;
    }

    public static int[] mapArrayToPixelHeight(DrawContext context, double[] data) {
        int[] pixelHeights = new int[data.length];
        for (int i = 0; i < data.length; i++) pixelHeights[i] = (int) (data[i] * getImageHeight(context));
        return pixelHeights;
    }
}
