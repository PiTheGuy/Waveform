package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.io.DrawContext;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class ReferenceLineGraphDrawer extends LineGraphDrawer {
    public ReferenceLineGraphDrawer(DrawContext context) {
        super(context);
    }

    public BufferedImage drawDataWithReferenceLine(double[] data) {
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        g.setColor(HeatmapDrawer.getColor(0.5));
        g.drawLine(0, getImageHeight(context) / 2, getImageWidth(), getImageHeight(context) / 2);
        drawData(g, data);
        return image;
    }

    @Override
    public BufferedImage drawFrame(double sec) {
        BufferedImage image = super.drawFrame(sec);
        Graphics2D g = image.createGraphics();
        g.setColor(HeatmapDrawer.getColor(0.5));
        int startX = (int) (sec / playingAudio.duration() * getImageWidth());
        g.drawLine(startX, getImageHeight(context) / 2, getImageWidth(), getImageHeight(context) / 2);
        return image;
    }

    @Override
    public BufferedImage drawFullAudio() {
        BufferedImage image = super.drawFullAudio();
        Graphics2D g = image.createGraphics();
        g.setColor(HeatmapDrawer.getColor(0.5));
        g.drawLine(0, getImageHeight(context) / 2, getImageWidth(), getImageHeight(context) / 2);
        return image;
    }

    @Override
    public boolean usesDynamicIcon() {
        return false;
    }
}
