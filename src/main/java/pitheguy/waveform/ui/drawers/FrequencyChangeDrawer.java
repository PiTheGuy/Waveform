package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.BitSet;

public class FrequencyChangeDrawer extends AudioDrawer {
    double[] previousMagnitudes;
    private DrawDirection drawDirection;

    public FrequencyChangeDrawer(DrawContext context) {
        super(context);
    }

    @Override
    public BufferedImage drawFrame(double sec) {
        super.drawFrame(sec);
        short[] monoData = AudioData.averageChannels(left, right);
        double[] rawMagnitudes = FftAnalyser.performFFT(Util.normalize(monoData));
        double[] magnitudes = Util.normalize(FftAnalyser.resampleMagnitudesToBands(rawMagnitudes, getNumLines()));
        if (previousMagnitudes == null || previousMagnitudes.length != Math.min(getNumLines(), magnitudes.length))
            previousMagnitudes = new double[getNumLines()];
        BitSet increasedFrequencies = new BitSet(getNumLines());
        if (previousMagnitudes == null) return createBlankImage();
        double requiredDifference = getSetting("required_difference", Double.class);
        for (int i = 0; i < magnitudes.length; i++)
            if (Math.abs(magnitudes[i] - previousMagnitudes[i]) > requiredDifference) increasedFrequencies.set(i);
        previousMagnitudes = magnitudes;
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        g.setColor(Config.foregroundColor());
        double scale = (double) getNumLines() / magnitudes.length;
        for (int i = 0; i < magnitudes.length; i++) {
            int startIndex = (int) (i * scale);
            int endIndex = (int) ((i + 1) * scale) - 1;
            if (increasedFrequencies.get(i)) for (int index = startIndex; index <= endIndex; index++)
                drawLine(g, index);
        }
        return image;
    }

    private int getNumLines() {
        return switch (drawDirection) {
            case HORIZONTAL -> context.getWidth();
            case VERTICAL -> context.getHeight();
            case RINGS -> Math.min(context.getWidth(), context.getHeight());
        };
    }

    private void drawLine(Graphics2D g, int index) {
        switch (drawDirection) {
            case HORIZONTAL -> g.drawLine(index, 0, index, context.getHeight());
            case VERTICAL -> g.drawLine(0, index, context.getWidth(), index);
            case RINGS -> CircularDrawer.drawRing(context, g, index, 1);
        }
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        previousMagnitudes = null;
        drawDirection = getSetting("draw_direction", DrawDirection.class);
    }

    @Override
    public void regenerateIfNeeded() {
        previousMagnitudes = null;
        drawDirection = getSetting("draw_direction", DrawDirection.class);
    }

    @Override
    public boolean shouldShowEpilepsyWarning() {
        return true;
    }

    @Override
    public boolean supportsPlayerMode() {
        return false;
    }

    @Override
    public SettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("draw_direction", SettingType.forEnum(DrawDirection.class), DrawDirection.RINGS)
                .addSetting("required_difference", SettingType.fraction(), 0.1);
    }

    private enum DrawDirection {
        HORIZONTAL("Horizontal"),
        VERTICAL("Vertical"),
        RINGS("Rings"),;

        private final String name;

        DrawDirection(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
