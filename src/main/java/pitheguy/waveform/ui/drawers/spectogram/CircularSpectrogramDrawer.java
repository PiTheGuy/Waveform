package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.SettingType;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.ui.drawers.*;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CircularSpectrogramDrawer extends AudioDrawer {
    private BufferedImage image;
    private boolean invertAxes;

    public CircularSpectrogramDrawer(DrawContext context) {
        super(context);
    }

    @Override
    public BufferedImage drawFullAudio() {
        return image;
    }

    @Override
    public BufferedImage drawFrame(double sec) {
        super.drawFrame(sec);
        BufferedImage result = createBlankImage();
        for (int x = 0; x < result.getWidth(); x++) {
            for (int y = 0; y < result.getHeight(); y++) {
                if (shouldDrawPixel(sec, x, y)) {
                    int rgb = image.getRGB(x, y);
                    result.setRGB(x, y, rgb);
                }
            }
        }
        return result;
    }

    private boolean shouldDrawPixel(double sec, int x, int y) {
        double percentPlayed = sec / playingAudio.duration();
        if (invertAxes) {
            CircularDrawer.PolarCoordinates polarCoordinates = CircularDrawer.getPolarCoordinates(context, x, y);
            return polarCoordinates.theta() < percentPlayed * 2 * Math.PI;
        } else {
            int centerX = getImageWidth() / 2;
            int centerY = getImageHeight() / 2;
            double dx = x - centerX;
            double dy = y - centerY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            return distance < percentPlayed * getImageSize() / 2;
        }
    }

    private BufferedImage makeImage() {
        double[][] frequencyData = FftAnalyser.getFrequencyData(playingAudio.getMonoData(), getImageSize());
        BufferedImage image = createBlankImage();
        for (int time = 0; time < frequencyData.length; time++) {
            for (int band = 0; band < frequencyData[time].length; band++) {
                double normTime = (double) time / frequencyData.length;
                double normBand = (double) band / frequencyData[time].length;
                Point point = getDisplayPoint(normTime, normBand);
                if (point.x >= 0 && point.x < image.getWidth() &&
                    point.y >= 0 && point.y < image.getHeight()) {
                    image.setRGB(point.x, point.y, HeatmapDrawer.getColor(frequencyData[time][band]).getRGB());
                }
            }
        }
        return image;
    }

    private Point getDisplayPoint(double normTime, double normBand) {
        if (invertAxes) return CircularDrawer.getDrawPoint(context, normBand, normTime * 2 * Math.PI);
        else return CircularDrawer.getDrawPoint(context, normTime, normBand * 2 * Math.PI);
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        invertAxes = getSetting("invert_axes", Boolean.class);
        image = makeImage();
    }

    @Override
    public void regenerateIfNeeded() {
        invertAxes = getSetting("invert_axes", Boolean.class);
        image = makeImage();
    }

    private int getImageSize() {
        return Math.min(getImageWidth(), getImageHeight());
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("invert_axes", SettingType.bool(), false);
    }
}