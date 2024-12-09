package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.main.Visualizer;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.SettingType;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.ui.util.DebugText;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;
import pitheguy.waveform.util.rolling.RollingList;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class BallDrawer extends AudioDrawer {
    private static final int HISTORY_SIZE = 200;
    private double delta;

    private final RollingList<Double> history = new RollingList<>(HISTORY_SIZE);

    public BallDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    @Override
    public Visualizer getVisualizer() {
        return Visualizer.BALL;
    }

    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        super.drawAudio(sec, length);
        short[] data = AudioData.averageChannels(left, right);
        double[] magnitudes = FftAnalyser.performFFT(Util.normalize(data));
        double energy = Arrays.stream(magnitudes).sum();
        history.add(energy);
        double cutoff = getCutoff();
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Config.foregroundColor);
        int imageSize = Math.min(Waveform.WIDTH, Waveform.HEIGHT);
        int smallRadius = (int) (imageSize * getSetting("small_radius", Double.class));
        int bigRadius = (int) (imageSize * getSetting("big_radius", Double.class));
        double delta = getDelta(energy > cutoff);
        int radius = (int) Util.lerp(delta, smallRadius, bigRadius);
        int startX = Waveform.WIDTH / 2 - radius;
        int startY = Waveform.HEIGHT / 2 - radius;
        g.fillOval(startX, startY, radius * 2, radius * 2);
        drawDebugText(g, new DebugText().add("Energy", energy).add("Cutoff", cutoff).add("Delta", delta));
        return image;
    }

    private double getCutoff() {
        double average = history.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        if (getSetting("increased_sensitivity", Boolean.class)) return average;
        return average + stdDev(history.stream().mapToDouble(Double::doubleValue).toArray(), average);
    }

    private double stdDev(double[] data, double average) {
        return Math.sqrt(Arrays.stream(data).map(value -> (value - average) * (value - average)).average().orElseThrow());
    }

    private double getDelta(boolean isPeak) {
        double persistence = getSetting("persistence", Double.class);
        if (isPeak) delta = 1;
        else delta = Math.max(delta - 1 / (persistence * Config.frameRate), 0);
        return smooth(delta);
    }

    private static double smooth(double delta) {
        if (delta < 0.5) return 4 * delta * delta * delta;
        else return (delta - 1) * Math.pow(2 * delta - 2, 2) + 1;
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

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("small_radius", SettingType.fraction(), 0.15)
                .addSetting("big_radius", SettingType.fraction(), 0.25)
                .addSetting("persistence", SettingType.fraction(), 0.2)
                .addSetting("increased_sensitivity", SettingType.bool(), false);
    }
}
