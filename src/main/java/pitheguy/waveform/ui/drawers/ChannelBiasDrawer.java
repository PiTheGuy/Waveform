package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.main.Visualizer;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.config.visualizersettings.options.VisualizationMode;
import pitheguy.waveform.ui.util.DebugText;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ChannelBiasDrawer extends CompoundDrawer {
    public ChannelBiasDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected AudioDrawer getDrawer() {
        VisualizationMode mode = getSetting("visualization_mode", VisualizationMode.class);
        return switch (mode) {
            case INSTANTANEOUS -> new Instantaneous(context);
            case GRAPH -> new Graph(context);
        };
    }

    public static double getBias(double leftRMS, double rightRMS) {
        if (leftRMS + rightRMS == 0) return 0;
        return -((leftRMS - rightRMS) / (leftRMS + rightRMS));
    }

    public static class Instantaneous extends SmoothedAudioDrawer {
        public Instantaneous(DrawContext context) {
            super(context, 10, false);
        }

        @Override
        protected BufferedImage drawAudio(double sec, double length) {
            super.drawAudio(sec, length);
            double leftRMS = VolumeDrawer.calculateRMS(Util.normalize(left));
            double rightRMS = VolumeDrawer.calculateRMS(Util.normalize(right));
            double bias = getBias(leftRMS, rightRMS);
            double displayValue = getDisplayValue(bias);
            int ballSize = (int) (Math.min(context.getWidth(), context.getHeight()) * 0.2);
            BufferedImage image = createBlankImage();
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Config.foregroundColor);
            int ballX = (int) ((displayValue + 1) / 2 * context.getWidth());
            int ballY = context.getHeight() / 2;
            drawBall(g, ballX, ballY, ballSize);
            drawDebugText(g, new DebugText().add("Left", leftRMS).add("Right", rightRMS).add("Bias", bias).add("Displayed", displayValue));
            return image;
        }

        private static void drawBall(Graphics2D g, int x, int y, int size) {
            int startX = x - size / 2;
            int startY = y - size / 2;
            g.fillOval(startX, startY, size, size);
        }

        @Override
        public boolean isSeekingAllowed() {
            return false;
        }

        @Override
        public Visualizer getVisualizer() {
            return Visualizer.CHANNEL_BIAS;
        }
    }

    public static class Graph extends ReferenceLineGraphDrawer {
        public Graph(DrawContext context) {
            super(context);
        }

        @Override
        protected BufferedImage precomputeImage() {
            short[][] leftData = HeatmapDrawer.getSlicedAudioData(context, playingAudio.left());
            short[][] rightData = HeatmapDrawer.getSlicedAudioData(context, playingAudio.right());
            double[] biases = new double[leftData.length];
            for (int x = 0; x < leftData.length; x++) {
                double leftRMS = VolumeDrawer.calculateRMS(Util.normalize(leftData[x]));
                double rightRMS = VolumeDrawer.calculateRMS(Util.normalize(rightData[x]));
                double bias = ChannelBiasDrawer.getBias(leftRMS, rightRMS);
                biases[x] = bias;
            }
            double[] points = Arrays.stream(biases).map(bias -> (bias + 1) / 2).toArray();
            return drawDataWithReferenceLine(points);
        }

        @Override
        public Visualizer getVisualizer() {
            return Visualizer.CHANNEL_BIAS;
        }
    }

    @Override
    public SettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("visualization_mode", SettingType.forEnum(VisualizationMode.class), VisualizationMode.INSTANTANEOUS);
    }

}
