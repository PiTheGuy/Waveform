package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.DrawContext;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public abstract class CircularDrawer extends AudioDrawer {
    public static final int MAX_DOUBLE_UP_LENGTH = 5000;

    public CircularDrawer(DrawContext context) {
        super(context);
    }

    public void updatePlayed(BufferedImage image, double seconds, double duration) {
        int centerX = context.getWidth() / 2;
        int centerY = context.getHeight() / 2;
        double progress = seconds / duration;
        int fromRGB = Config.foregroundColor().getRGB();
        int toRGB = Config.playedColor().getRGB();
        double maxRadius = Math.min(centerX, centerY);
        double maxAngle = progress * 2 * Math.PI;
        for (double angle = -Math.PI / 2; angle <= maxAngle - Math.PI / 2; angle += 0.001) {
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            for (double r = 0; r < maxRadius; r += 0.5) {
                int x = (int) (centerX + r * cos);
                int y = (int) (centerY + r * sin);
                if (x >= 0 && x < context.getWidth() && y >= 0 && y < context.getHeight() && image.getRGB(x, y) == fromRGB)
                    image.setRGB(x, y, toRGB);
            }
        }
    }

    protected static void drawLinePolar(DrawContext context, Graphics2D g, double r, double theta) {
        int centerX = context.getWidth() / 2;
        int centerY = context.getHeight() / 2;
        Point point = getDrawPoint(context, r, theta);
        g.drawLine(centerX, centerY, point.x, point.y);
    }

    public static Point getDrawPoint(DrawContext context, double r, double theta) {
        theta -= Math.PI / 2;
        int circleDiameter = Math.min(context.getWidth(), context.getHeight());
        int centerX = context.getWidth() / 2;
        int centerY = context.getHeight() / 2;
        int x = centerX + (int) (r * Math.cos(theta) * circleDiameter / 2);
        int y = centerY + (int) (r * Math.sin(theta) * circleDiameter / 2);
        return new Point(x, y);
    }

    public static PolarCoordinates getPolarCoordinates(DrawContext context, int x, int y) {
        int circleDiameter = Math.min(context.getWidth(), context.getHeight());
        int centerX = context.getWidth() / 2;
        int centerY = context.getHeight() / 2;
        double deltaX = x - centerX;
        double deltaY = y - centerY;
        double r = Math.sqrt(deltaX * deltaX + deltaY * deltaY) / ((double) circleDiameter / 2);
        double theta = Math.atan2(deltaY, deltaX) + Math.PI / 2;
        if (theta < 0) theta += 2 * Math.PI;
        return new PolarCoordinates(r, theta);
    }

    protected static void drawData(DrawContext context, Graphics2D g, double[] data) {
        int numBars = data.length > MAX_DOUBLE_UP_LENGTH ? data.length : data.length * 2;
        double radiansPerBar = Math.PI * 2 / numBars;

        double scale = (double) data.length / numBars;

        for (int i = 0; i < numBars; i++) {
            double fractionalIndex = i * scale;
            int lowerIndex = (int) Math.floor(fractionalIndex);
            int upperIndex = Math.min(lowerIndex + 1, data.length - 1);
            double weight = fractionalIndex - lowerIndex;
            double interpolatedValue = (1 - weight) * data[lowerIndex] + weight * data[upperIndex];
            drawLinePolar(context, g, interpolatedValue, i * radiansPerBar);
        }
    }

    public static void drawRing(Graphics2D g, int centerX, int centerY, int radius, double brightness) {
        Color color = HeatmapDrawer.getColor(brightness);
        g.setColor(color);
        g.drawOval(centerX - radius, centerY - radius, 2 * radius + 1, 2 * radius + 1);
    }

    public static void drawRing(DrawContext context, Graphics2D g, int radius, double brightness) {
        drawRing(g, context.getWidth() / 2, context.getHeight() / 2, radius, brightness);
    }

    @Override
    public boolean isSeekingAllowed() {
        return false;
    }

    public record PolarCoordinates(double r, double theta) {
    }
    public static List<Ring> combineRings(List<Ring> rings) {
        List<Ring> result = new ArrayList<>();
        Ring currentChain = null;
        for (Ring ring : rings) {
            if (ring.brightness < 1) {
                if (currentChain != null) {
                    result.add(currentChain);
                    currentChain = null;
                }
                result.add(ring);
            } else {
                if (currentChain == null) currentChain = new Ring(ring.startRadius, ring.endRadius, 1);
                else currentChain.endRadius = ring.endRadius;
            }
        }
        if (currentChain != null) result.add(currentChain);
        return result;
    }

    public static class Ring {
        public final int startRadius;
        public int endRadius;
        public final double brightness;

        public Ring(int startRadius, int endRadius, double brightness) {
            this.startRadius = startRadius;
            this.endRadius = endRadius;
            this.brightness = brightness;
        }

        public Ring(int radius, double brightness) {
            this(radius, radius + 1, brightness);
        }

        public void draw(DrawContext context, Graphics2D g) {
            if (brightness == 0) return;
            g.setStroke(new BasicStroke(endRadius - startRadius));
            CircularDrawer.drawRing(context, g, (startRadius + endRadius) / 2, brightness);
        }
    }
}
