package pitheguy.waveform.ui;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.drawers.*;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class BeatDetectionRipplesDrawer extends AbstractBeatDetectionDrawer {
    private final List<Ripple> ripples = new ArrayList<>();
    private boolean wasBeat = false;

    public BeatDetectionRipplesDrawer(DrawContext context) {
        super(context);
    }

    @Override
    public BufferedImage drawFrame(double sec) {
        super.drawFrame(sec);
        boolean beatDetected = isBeatDetected();
        if (beatDetected && !wasBeat) newRipple();
        wasBeat = beatDetected;
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        for (Ripple ripple : ripples) ripple.draw(g);
        ripples.removeIf(Ripple::isFinished);
        return image;
    }

    private void newRipple() {
        boolean allowEdge = getSetting("allow_edge", Boolean.class);
        double rippleSize = getSetting("end_radius", Double.class);
        double margin = allowEdge ? 0 : rippleSize;
        Random random = new Random();
        int attempts = 0;
        double x, y;
        do {
            x = random.nextDouble(margin, 1 - margin);
            y = random.nextDouble(margin, 1 - margin);
        } while (!isValidPoint(x, y) && attempts++ < 20);
        ripples.add(new Ripple(x, y));
    }

    private boolean isValidPoint(double x, double y) {
        boolean allowOverlap = getSetting("allow_overlap", Boolean.class);
        boolean allowEdge = getSetting("allow_edge", Boolean.class);
        double rippleSize = getSetting("end_radius", Double.class);
        if (!allowEdge) {
            if (x < rippleSize || x > 1 - rippleSize) return false;
            if (y < rippleSize || y > 1 - rippleSize) return false;
        }
        if (!allowOverlap)
            for (Ripple ripple : ripples) {
                double dx = x - ripple.x;
                double dy = y - ripple.y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance < rippleSize * 2) return false;
            }
        return true;
    }

    private int getRippleStartSize() {
        double startRadiusSetting = getSetting("start_radius", Double.class);
        return (int) (Math.min(context.getWidth(), context.getHeight()) * startRadiusSetting);
    }

    private int getRippleEndSize() {
        double endRadiusSetting = getSetting("end_radius", Double.class);
        return (int) (Math.min(context.getWidth(), context.getHeight()) * endRadiusSetting);
    }

    private int getRippleLifetime() {
        double lifetimeSec = getSetting("lifetime", Double.class);
        return (int) (lifetimeSec * Config.frameRate);
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        ripples.clear();
        wasBeat = false;
    }

    @Override
    public boolean supportsPlayerMode() {
        return false;
    }

    @Override
    public SettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("start_radius", SettingType.fraction(), 0.05)
                .addSetting("end_radius", SettingType.fraction(), 0.1)
                .addSetting("lifetime", SettingType.positiveDouble(), 0.5)
                .addSetting("allow_overlap", SettingType.BOOLEAN, false)
                .addSetting("allow_edge", SettingType.BOOLEAN, false);
    }

    private class Ripple {
        private final double x;
        private final double y;
        private int framesExisted = 0;
        private boolean finished = false;

        public Ripple(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void draw(Graphics2D g) {
            if (finished) return;
            int startSize = getRippleStartSize();
            int endSize = getRippleEndSize();
            int radius = Util.lerp((double) framesExisted / getRippleLifetime(), 0, endSize - startSize);
            int endRadius = Math.min(endSize, startSize + radius);
            g.setStroke(new BasicStroke(Math.max(0, endRadius - radius)));
            int centerX = (int) (x * context.getWidth());
            int centerY = (int) (y * context.getHeight());
            CircularDrawer.drawRing(g, centerX, centerY, (radius + endRadius) / 2, 1);
            if (framesExisted++ > getRippleLifetime() * 2) finished = true;
        }

        public boolean isFinished() {
            return finished;
        }
    }
}
