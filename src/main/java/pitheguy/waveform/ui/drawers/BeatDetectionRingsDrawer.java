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

public class BeatDetectionRingsDrawer extends AbstractBeatDetectionDrawer {
    private final RollingList<Boolean> pastPeaks = new DynamicRollingList<>(this::getNumRings);

    public BeatDetectionRingsDrawer(DrawContext context) {
        super(context);
    }

    @Override
    public BufferedImage drawFullAudio() {
        super.drawFullAudio();
        int numRings = getNumRings();
        double[][] frequencyData = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), numRings);
        double[] energy = Arrays.stream(frequencyData).mapToDouble(arr -> Arrays.stream(arr).sum()).toArray();
        int historySize = (int) (numRings * 0.2);
        RollingList<Double> history = new RollingList<>(historySize);
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        for (int ring = 0; ring < numRings; ring++) {
            history.add(energy[ring]);
            double cutoff = getCutoff(history, false);
            if (energy[ring] > cutoff) CircularDrawer.drawRing(context, g, ring, 1);
        }
        return image;
    }

    @Override
    public BufferedImage drawFrame(double sec) {
        super.drawFrame(sec);
        pastPeaks.add(isBeatDetected());
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        for (int i = 0; i < pastPeaks.size(); i++)
            if (pastPeaks.get(pastPeaks.size() - 1 - i)) CircularDrawer.drawRing(context, g, i, 1);
        return image;
    }

    private int getNumRings() {
        return Math.min(context.getWidth(), context.getHeight()) / 2;
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        pastPeaks.clear();
    }
}
