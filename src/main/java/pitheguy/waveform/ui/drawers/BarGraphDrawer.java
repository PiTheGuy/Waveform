package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.ui.Waveform;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class BarGraphDrawer extends AudioDrawer {
    private final boolean reverseDirection;

    public BarGraphDrawer(boolean forceFullAudio, boolean reverseDirection) {
        super(forceFullAudio);
        this.reverseDirection = reverseDirection;
    }

    protected BufferedImage drawArray(double[] data, BufferedImage image) {
        return drawArray(data, image, Config.foregroundColor);
    }


    protected BufferedImage drawArray(double[] data, BufferedImage image, Color color) {
        return drawArray(data, image, color, reverseDirection);
    }

    public static BufferedImage drawArray(double[] data, BufferedImage image, boolean reverseDirection) {
        return drawArray(data, image, Config.foregroundColor, reverseDirection);
    }

    public static BufferedImage drawArray(double[] data, BufferedImage image, Color color, boolean reverseDirection) {
        int[] pixelHeights = mapArrayToPixelHeight(data);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        double scale = (double) data.length / Waveform.WIDTH;
        for (int x = 0; x < Waveform.WIDTH; x++) {
            int index = (int) (x * scale);
            int startY = reverseDirection ? 0 : Waveform.HEIGHT - 1;
            int endY = reverseDirection ? pixelHeights[index] : Waveform.HEIGHT - 1 - pixelHeights[index];
            g.drawLine(x, startY, x, endY);
        }
        g.dispose();
        return image;
    }

    public static int[] mapArrayToPixelHeight(double[] data) {
        int[] pixelHeights = new int[data.length];
        for (int i = 0; i < data.length; i++) pixelHeights[i] = (int) (data[i] * Waveform.HEIGHT);
        return pixelHeights;
    }
}
