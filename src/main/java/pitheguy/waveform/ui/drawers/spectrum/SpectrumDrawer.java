package pitheguy.waveform.ui.drawers.spectrum;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.SettingType;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.ui.drawers.*;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SpectrumDrawer extends CompoundDrawer {

    public SpectrumDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected AudioDrawer getDrawer() {
        return switch(getSetting("display_mode", DisplayMode.class)) {
            case BAR_GRAPH -> new BarGraph(context);
            case LINE_GRAPH -> new LineGraph(context);
            case DOT_PLOT -> new DotPlot(context);
        };
    }

    protected double[] getDisplayData(double sec, double length) {
        updateAudioData(sec, length);
        short[] monoData = AudioData.averageChannels(left, right);
        double[] magnitudes = FftAnalyser.performFFT(Util.normalize(monoData));
        return Util.normalize(magnitudes);
    }

    private Point getDisplayPoint(double[] displayData, int index) {
        boolean invertDirection = getSetting("invert_direction", Boolean.class);
        int pixelHeight = (int) (displayData[index] * context.getHeight() - 1);
        int x = (int) ((double) index / displayData.length * context.getWidth());
        int y = invertDirection ? context.getHeight() - 1 - pixelHeight : pixelHeight;
        return new Point(x, y);
    }

    private class BarGraph extends BarGraphDrawer {
        public BarGraph(DrawContext context) {
            super(context, !SpectrumDrawer.this.getSetting("invert_direction", Boolean.class));
        }
        @Override
        protected BufferedImage drawAudio(double sec, double length) {
            super.drawAudio(sec, length);
            double[] displayData = getDisplayData(sec, length);
            return drawArray(displayData);
        }
    }

    private class LineGraph extends AudioDrawer {
        public LineGraph(DrawContext context) {
            super(context);
        }

        @Override
        protected BufferedImage drawAudio(double sec, double length) {
            super.drawAudio(sec, length);
            double[] displayData = getDisplayData(sec, length);
            BufferedImage image = createBlankImage();
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Point prevPoint = null;
            if (displayData.length < context.getWidth()) {
                for (int i = 0; i < displayData.length; i++) {
                    Point point = getDisplayPoint(displayData, i);
                    if (prevPoint != null) g.drawLine(prevPoint.x, prevPoint.y, point.x, point.y);
                    prevPoint = point;
                }
            } else {
                double scale = Math.max((double) displayData.length / context.getWidth(), 1.0);
                for (int x = 0; x < context.getWidth(); x++) {
                    int index = (int) (x * scale);
                    Point point = getDisplayPoint(displayData, index);
                    if (prevPoint != null) g.drawLine(prevPoint.x, prevPoint.y, point.x, point.y);
                    prevPoint = point;
                }
            }
            g.dispose();
            return image;
        }
    }

    private class DotPlot extends AudioDrawer {
        public DotPlot(DrawContext context) {
            super(context);
        }

        @Override
        protected BufferedImage drawAudio(double sec, double length) {
            super.drawAudio(sec, length);
            double[] displayData = getDisplayData(sec, length);
            BufferedImage image = createBlankImage();
            double scale = (double) displayData.length / context.getWidth();
            for (int x = 0; x < context.getWidth(); x++) {
                int index = (int) (x * scale);
                Point point = getDisplayPoint(displayData, index);
                image.setRGB(point.x, point.y, Config.foregroundColor.getRGB());
            }
            return image;
        }
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("display_mode", SettingType.forEnum(DisplayMode.class), DisplayMode.BAR_GRAPH)
                .addSetting("invert_direction", SettingType.bool(), false);
    }

    private enum DisplayMode {
        BAR_GRAPH("Bar Graph"),
        LINE_GRAPH("Line Graph"),
        DOT_PLOT("Dot Plot");

        private final String name;

        DisplayMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
