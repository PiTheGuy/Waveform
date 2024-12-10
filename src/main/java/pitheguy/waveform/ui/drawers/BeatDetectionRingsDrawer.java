package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.util.BeatDetectionHelper;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;
import pitheguy.waveform.util.rolling.DynamicRollingList;
import pitheguy.waveform.util.rolling.RollingList;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class BeatDetectionRingsDrawer extends AudioDrawer {
    private static final int HISTORY_SIZE = 200;
    private final RollingList<Double> history = new RollingList<>(HISTORY_SIZE);
    private final RollingList<Boolean> pastPeaks = new DynamicRollingList<>(this::getNumRings);

    public BeatDetectionRingsDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        super.drawAudio(sec, length);
        short[] data = AudioData.averageChannels(left, right);
        double[] magnitudes = FftAnalyser.performFFT(Util.normalize(data));
        double energy = Arrays.stream(magnitudes).sum();
        history.add(energy);
        double cutoff = BeatDetectionHelper.getCutoff(history, false);
        pastPeaks.add(energy > cutoff);
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        for (int i = 0; i < pastPeaks.size(); i++) {
            if (pastPeaks.get(pastPeaks.size() - 1 - i)) {
                CircularDrawer.drawRing(context, g, i, 1);
            }
        }
        return image;
    }

    private int getNumRings() {
        return Math.min(getImageWidth(), getImageHeight(context)) / 2;
    }
}
