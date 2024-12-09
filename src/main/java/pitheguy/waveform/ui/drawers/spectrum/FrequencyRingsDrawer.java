package pitheguy.waveform.ui.drawers.spectrum;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.drawers.AudioDrawer;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class FrequencyRingsDrawer extends AudioDrawer {
    public FrequencyRingsDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        super.drawAudio(sec, length);
        double[] frequencyData = FftAnalyser.performFFT(Util.normalize(AudioData.averageChannels(left, right)));
        int numRings = Math.min(Waveform.WIDTH, Waveform.HEIGHT);
        double[] magnitudes = FftAnalyser.resampleMagnitudesToBands(frequencyData, numRings);
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        for (int ring = 0; ring < numRings; ring++) {
            drawRing(g, ring, magnitudes[ring]);
        }
        return image;
    }

    private static void drawRing(Graphics2D g, int radius, double brightness) {
        Color color = HeatmapDrawer.getColor(brightness);
        int centerX = Waveform.WIDTH / 2;
        int centerY = Waveform.HEIGHT / 2;
        g.setColor(color);
        g.drawOval(centerX - radius, centerY - radius, 2 * radius + 1, 2 * radius + 1);
    }

    @Override
    public boolean shouldShowEpilepsyWarning() {
        return true;
    }

    @Override
    public boolean isSeekingAllowed() {
        return Config.playerMode;
    }
}
