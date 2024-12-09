package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.ui.Waveform;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class ReferenceLineGraphDrawer extends LineGraphDrawer {
    public ReferenceLineGraphDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    public BufferedImage drawDataWithReferenceLine(double[] data) {
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        g.setColor(HeatmapDrawer.getColor(0.5));
        g.drawLine(0, Waveform.HEIGHT / 2, Waveform.WIDTH, Waveform.HEIGHT / 2);
        drawData(g, data);
        return image;
    }

    @Override
    public BufferedImage drawFrame(double sec) {
        BufferedImage image = super.drawFrame(sec);
        Graphics2D g = image.createGraphics();
        g.setColor(HeatmapDrawer.getColor(0.5));
        int startX = (int) (sec / playingAudio.duration() * Waveform.WIDTH);
        g.drawLine(startX, Waveform.HEIGHT / 2, Waveform.WIDTH, Waveform.HEIGHT / 2);
        return image;
    }

    @Override
    public BufferedImage drawFullAudio() {
        BufferedImage image = super.drawFullAudio();
        Graphics2D g = image.createGraphics();
        g.setColor(HeatmapDrawer.getColor(0.5));
        g.drawLine(0, Waveform.HEIGHT / 2, Waveform.WIDTH, Waveform.HEIGHT / 2);
        return image;
    }

    @Override
    public boolean usesDynamicIcon() {
        return false;
    }
}
