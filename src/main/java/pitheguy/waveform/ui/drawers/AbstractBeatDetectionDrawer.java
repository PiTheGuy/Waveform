package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;
import pitheguy.waveform.util.rolling.RollingList;

import java.util.Arrays;
import java.util.Collection;

public abstract class AbstractBeatDetectionDrawer extends AudioDrawer {
    private static final int HISTORY_SIZE = 200;
    private final RollingList<Double> history = new RollingList<>(HISTORY_SIZE);

    public AbstractBeatDetectionDrawer(DrawContext context) {
        super(context);
    }

    public boolean isBeatDetected() {
        short[] data = AudioData.averageChannels(left, right);
        double[] magnitudes = FftAnalyser.performFFT(Util.normalize(data));
        double energy = Arrays.stream(magnitudes).sum();
        history.add(energy);
        double cutoff = getCutoff(history, extraSensitivity());
        return energy > cutoff;
    }

    public boolean extraSensitivity() {
        return false;
    }

    public static double getCutoff(Collection<Double> history, boolean increasedSensitivity) {
        double average = history.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        if (increasedSensitivity) return average;
        return average + stdDev(history.stream().mapToDouble(Double::doubleValue).toArray(), average);
    }

    private static double stdDev(double[] data, double average) {
        return Math.sqrt(Arrays.stream(data).map(value -> (value - average) * (value - average)).average().orElseThrow());
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        history.clear();
    }
}
