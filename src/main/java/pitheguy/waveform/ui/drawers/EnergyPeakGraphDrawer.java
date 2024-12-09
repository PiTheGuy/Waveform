package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.util.DebugText;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;
import pitheguy.waveform.util.rolling.RollingList;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class EnergyPeakGraphDrawer extends BarGraphDrawer {
    private static final int HISTORY_SIZE = 200;
    private final RollingList<Double> history = new RollingList<>(HISTORY_SIZE);
    private double delta = 0.0;

    public EnergyPeakGraphDrawer(boolean forceFullAudio) {
        super(forceFullAudio, true);
    }

    
    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        updateAudioData(sec, length);
        short[] data = AudioData.averageChannels(left, right);
        double[] magnitudes = FftAnalyser.performFFT(Util.normalize(data));
        double energy = Arrays.stream(magnitudes).sum();
        history.add(energy);
        double average = history.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
        double cutoff = average + stdDev(history.stream().mapToDouble(Double::doubleValue).toArray(), average);
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        double[] displayData = history.stream().mapToDouble(v -> v / 10000).toArray();
        delta = Math.min(Math.max(0, energy > cutoff ? delta + 0.2 : delta - 0.2), 1);
        drawArray(displayData, image, Util.blendColor(delta, Config.foregroundColor, Config.playedColor));

        g.setColor(Config.playedColor);
        g.drawLine(0, (int) (cutoff / 10000 * Waveform.HEIGHT), Waveform.WIDTH, (int) (cutoff / 10000 * Waveform.HEIGHT));
        drawDebugText(g, new DebugText().add("Cutoff", cutoff).add("Energy", energy));
        return image;
    }

    private double stdDev(double[] data, double average) {
        return Math.sqrt(Arrays.stream(data).map(value -> (value - average) * (value - average)).average().orElseThrow());
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        history.clear();
    }

    @Override
    public boolean isSeekingAllowed() {
        return false;
    }
}
