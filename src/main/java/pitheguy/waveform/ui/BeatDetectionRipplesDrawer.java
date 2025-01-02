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
        Point point = getRandomPoint();
        ripples.add(new Ripple(point.x, point.y));
    }

    private Point getRandomPoint() {
        boolean allowEdge = getSetting("allow_edge", Boolean.class);
        int rippleSize = getRippleEndSize();
        int margin = allowEdge ? 0 : rippleSize;
        Random random = new Random();
        Point point;
        int attempts = 0;
        do {
            int x = random.nextInt(margin, context.getWidth() - margin);
            int y = random.nextInt(margin, context.getHeight() - margin);
            point = new Point(x, y);
        } while (!isValidPoint(point) && attempts++ < 20);
        return point;
    }

    private boolean isValidPoint(Point point) {
        boolean allowOverlap = getSetting("allow_overlap", Boolean.class);
        boolean allowEdge = getSetting("allow_edge", Boolean.class);
        int rippleSize = getRippleEndSize();
        if (!allowEdge) {
            if (point.x < rippleSize || point.x > context.getWidth() - rippleSize) return false;
            if (point.y < rippleSize || point.y > context.getHeight() - rippleSize) return false;
        }
        if (!allowOverlap)
            for (Ripple ripple : ripples)
                if (point.distance(ripple.x, ripple.y) < rippleSize * 2) return false;
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
        private final int x;
        private final int y;
        private int framesExisted = 0;
        private boolean finished = false;

        public Ripple(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void draw(Graphics2D g) {
            if (finished) return;
            int startSize = getRippleStartSize();
            int endSize = getRippleEndSize();
            int radius = Util.lerp((double) framesExisted / getRippleLifetime(), 0, endSize - startSize);
            for (int r = radius; r < Math.min(startSize * 2, startSize + radius); r++)
                CircularDrawer.drawRing(g, x, y, r, 1);
            if (framesExisted++ > endSize) finished = true;
        }

        public boolean isFinished() {
            return finished;
        }
    }
}
