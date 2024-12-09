package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.*;
import pitheguy.waveform.ui.util.DebugText;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PolarityDrawer extends CompoundDrawer {
    public PolarityDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    @Override
    protected AudioDrawer getDrawer() {
        VisualizationMode mode = getSetting("visualization_mode", VisualizationMode.class);
        return switch (mode) {
            case INSTANTANEOUS -> new Instantaneous(forceFullAudio);
            case GRAPH -> new Graph(forceFullAudio);
        };
    }

    private static Polarity getPolarity(short[] audioData) {
        int positive = 0;
        int negative = 0;
        for (short sample : audioData) {
            if (sample > 0) positive++;
            else if (sample < 0) negative++;
        }
        double percentPositive = positive + negative == 0 ? 0.5 : (double) positive / (positive + negative);
        return new Polarity(positive, negative, percentPositive);
    }

    public static class Instantaneous extends AudioDrawer {
        public Instantaneous(boolean forceFullAudio) {
            super(forceFullAudio);
        }

        @Override
        protected BufferedImage drawAudio(double sec, double length) {
            super.drawAudio(sec, length);
            short[] audioData = AudioData.averageChannels(left, right);
            Polarity polarity = getPolarity(audioData);
            BufferedImage image = createBlankImage();
            Graphics2D g = image.createGraphics();
            g.setColor(Config.foregroundColor);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int ballSize = (int) (Math.min(Waveform.WIDTH, Waveform.HEIGHT) * 0.2);
            int ballX = Waveform.WIDTH / 2;
            int ballY = (int) (polarity.percentPositive() * Waveform.HEIGHT);
            drawBall(g, ballX, ballY, ballSize);
            drawDebugText(g, new DebugText().add("Positive", polarity.positive()).add("Negative", polarity.negative()).add("Displayed", polarity.percentPositive()));
            return image;
        }

        private static void drawBall(Graphics2D g, int x, int y, int size) {
            int startX = x - size / 2;
            int startY = y - size / 2;
            g.fillOval(startX, startY, size, size);
        }
    }

    public static class Graph extends ReferenceLineGraphDrawer {
        public Graph(boolean forceFullAudio) {
            super(forceFullAudio);
        }

        @Override
        protected BufferedImage precomputeImage() {
            short[][] audioDataSlices = HeatmapDrawer.getSlicedAudioData(playingAudio.getMonoData());
            double[] displayData = new double[audioDataSlices.length];
            for (int i = 0; i < audioDataSlices.length; i++) {
                short[] audioData = audioDataSlices[i];
                Polarity polarity = getPolarity(audioData);
                displayData[i] = polarity.percentPositive();
            }
            return drawDataWithReferenceLine(displayData);
        }
    }

    private enum VisualizationMode {
        INSTANTANEOUS("Instantaneous"),
        GRAPH("Graph");

        private final String name;

        VisualizationMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("visualization_mode", SettingType.forEnum(VisualizationMode.class), VisualizationMode.INSTANTANEOUS);
    }

    private record Polarity(int positive, int negative, double percentPositive) {
    }
}