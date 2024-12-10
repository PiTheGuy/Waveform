package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.DrawContext;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class RangeLineGraphDrawer extends SlicedImageDrawer {
    public RangeLineGraphDrawer(DrawContext context) {
        super(context);
    }

    protected BufferedImage drawData(double[] centers, double[] deviations) {
        BufferedImage image = createBlankImage();
        double previousCenter = 0, previousDeviation = 0;
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(HeatmapDrawer.getColor(0.5));
        for (int x = 0; x < getImageWidth(); x++) {
            double deviation = deviations[x];
            double center = centers[x];
            if (x == 0) g.drawLine(x, mapToPixelHeight(center - deviation), x, mapToPixelHeight(center + deviation));
            else {
                Polygon polygon = new Polygon();
                polygon.addPoint(x, mapToPixelHeight(center - deviation));
                polygon.addPoint(x, mapToPixelHeight(center + deviation));
                polygon.addPoint(x - 1, mapToPixelHeight(previousCenter - previousDeviation));
                polygon.addPoint(x - 1, mapToPixelHeight(previousCenter + previousDeviation));
                g.fillPolygon(polygon);
            }
            previousCenter = center;
            previousDeviation = deviation;
        }
        g.setColor(Config.foregroundColor);
        for (int x = 0; x < getImageWidth(); x++) {
            double center = centers[x];
            int y = mapToPixelHeight(center);
            if (x != 0) g.drawLine(x, y, x - 1, mapToPixelHeight(previousCenter));
            previousCenter = center;
        }
        g.dispose();
        return image;
    }

    private int mapToPixelHeight(double value) {
        return (int) (value * getImageHeight(context));
    }
}
