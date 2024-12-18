package pitheguy.waveform.ui.drawers.spectrum;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.ui.drawers.AudioDrawer;
import pitheguy.waveform.ui.drawers.CircularDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class FrequencyOrbitsDrawer extends AudioDrawer {
    public static final double ORBIT_SIZE = 100;
    public static final int FULL_AUDIO_ITERATIONS = 1000;
    private double[] positions;
    private BufferedImage image;

    public FrequencyOrbitsDrawer(DrawContext context) {
        super(context);
    }

    @Override
    public BufferedImage drawFullAudio() {
        double[][] frequencyData = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), FULL_AUDIO_ITERATIONS);
        double[][] magnitudes = new double[context.getWidth()][];
        Arrays.setAll(magnitudes, i -> FftAnalyser.resampleMagnitudesToBands(frequencyData[i], positions.length));
        double speed = getSetting("speed", Double.class);
        for (int time = 0; time < frequencyData.length; time++)
            for (int i = 0; i < positions.length; i++) positions[i] += magnitudes[time][i] * speed;
        BufferedImage image = createBlankImage();
        drawData(image);
        return image;
    }

    @Override
    public BufferedImage drawFrame(double sec) {
        if (positions == null) return createBlankImage();
        super.drawFrame(sec);
        double[] frequencyData = FftAnalyser.performFFT(Util.normalize(AudioData.averageChannels(left, right)));
        double[] magnitudes = FftAnalyser.resampleMagnitudesToBands(frequencyData, positions.length);
        double speed = getSetting("speed", Double.class);
        for (int i = 0; i < positions.length; i++) positions[i] += magnitudes[i] * speed;
        if (getSetting("show_trails", Boolean.class)) fadeTrails();
        else image = createBlankImage();
        drawData(image);
        return copyBufferedImage(image);
    }

    private void drawData(BufferedImage image) {
        PointSize pointSize = getSetting("point_size", PointSize.class);
        Graphics2D g = image.createGraphics();
        g.setColor(Config.foregroundColor);
        for (int index = 0; index < positions.length; index++) {
            double pos = (positions[index] % ORBIT_SIZE) / ORBIT_SIZE;
            Point point = convertToDisplayPoint(index, pos);
            pointSize.drawPoint(g, point.x, point.y);
        }
        g.dispose();
    }

    public static BufferedImage copyBufferedImage(BufferedImage original) {
        BufferedImage copy = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return copy;
    }

    private Point convertToDisplayPoint(int index, double position) {
        return switch (getSetting("orbit_path", OrbitPath.class)) {
            case HORIZONTAL -> new Point((int) (position * context.getWidth()), index);
            case VERTICAL -> new Point(index, (int) (position * context.getHeight()));
            case CIRCULAR -> CircularDrawer.getDrawPoint(context, (double) index / positions.length, position * 2 * Math.PI);
        };
    }

    private void fadeTrails() {
        double fadeFactor = 0.5;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                if (rgb == Config.backgroundColor.getRGB()) continue;
                Color newColor = Util.blendColor(fadeFactor, Config.backgroundColor, new Color(rgb));
                image.setRGB(x, y, newColor.getRGB());
            }
        }
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        image = createBlankImage();
        int numBands = getNumBands();
        positions = new double[numBands];
        Arrays.fill(positions, 0);
    }

    @Override
    public void regenerateIfNeeded() {
        if (getNumBands() != positions.length) {
            positions = new double[getNumBands()];
            Arrays.fill(positions, 0);
        }
    }

    private int getNumBands() {
        return switch (getSetting("orbit_path", OrbitPath.class)) {
            case HORIZONTAL -> context.getHeight();
            case VERTICAL -> context.getWidth();
            case CIRCULAR -> Math.min(context.getWidth(), context.getHeight());
        };
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    public boolean isSeekingAllowed() {
        return false;
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("orbit_path", SettingType.forEnum(OrbitPath.class), OrbitPath.HORIZONTAL)
                .addSetting("speed", SettingType.positiveDouble(), 1.0)
                .addSetting("point_size", SettingType.forEnum(PointSize.class), PointSize.NORMAL)
                .addSetting("show_trails", SettingType.bool(), true);
    }

    private enum PointSize {
        NORMAL("Normal", 0),
        LARGE("Large", 1),
        VERY_LARGE("Very Large", 2);

        private final String name;
        private final int radius;

        PointSize(String name, int radius) {
            this.name = name;
            this.radius = radius;
        }

        @Override
        public String toString() {
            return name;
        }

        public void drawPoint(Graphics2D g, int x, int y) {
            g.fillRect(x - radius, y - radius, radius * 2 + 1, radius * 2 + 1);
        }
    }

    private enum OrbitPath {
        HORIZONTAL("Horizontal"),
        VERTICAL("Vertical"),
        CIRCULAR("Circular");

        private final String name;

        OrbitPath(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
