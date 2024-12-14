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
        g.drawLine(0, context.getHeight() / 2, context.getWidth(), context.getHeight() / 2);
        drawData(g, data);
        return image;
    }

    @Override
    public BufferedImage drawFrame(double sec) {
        BufferedImage image = super.drawFrame(sec);
        Graphics2D g = image.createGraphics();
        g.setColor(HeatmapDrawer.getColor(0.5));
        int startX = (int) (sec / playingAudio.duration() * context.getWidth());
        g.drawLine(startX, context.getHeight() / 2, context.getWidth(), context.getHeight() / 2);
        return image;
    }

    @Override
    public BufferedImage drawFullAudio() {
        BufferedImage image = super.drawFullAudio();
        Graphics2D g = image.createGraphics();
        g.setColor(HeatmapDrawer.getColor(0.5));
        g.drawLine(0, context.getHeight() / 2, context.getWidth(), context.getHeight() / 2);
        return image;
    }

    @Override
    public boolean usesDynamicIcon() {
        return false;
    }
}
