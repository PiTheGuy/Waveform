package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class LineGraphDrawer extends SlicedImageDrawer {
    public LineGraphDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    protected BufferedImage drawData(double[] data) {
        return drawData(data, Waveform.HEIGHT, 0, false);
    }

    protected BufferedImage drawData(double[] data, int height, int x, boolean reverse) {
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        drawData(g, data, height, x, reverse);
        g.dispose();
        return image;
    }


    protected void drawData(Graphics2D g, double[] data) {
        drawData(g, data, Waveform.HEIGHT, 0, false, Config.foregroundColor);
    }

    protected void drawData(Graphics2D g, double[] data, Color color) {
        drawData(g, data, Waveform.HEIGHT, 0, false, color);
    }

    protected void drawData(Graphics2D g, double[] data, int height, int x, boolean reverse) {
        drawData(g, data, height, x, reverse, Config.foregroundColor);
    }

    protected void drawData(Graphics2D g, double[] data, int height, int x, boolean reverse, Color color) {
        g.setColor(color);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(getSetting("line_thickness", Float.class)));
        double previousValue = 0;
        for (int currentX = 0; currentX < Waveform.WIDTH; currentX++) {
            double value = data[currentX];
            if (currentX != 0) g.drawLine(currentX, mapToPixelHeight(value, height, x, reverse), currentX - 1, mapToPixelHeight(previousValue, height, x, reverse));
            previousValue = value;
        }
    }

    private static int mapToPixelHeight(double value, int height, int x, boolean reverse) {
        int y = (int) (value * height) + x;
        return reverse ? Waveform.HEIGHT - 1 - y : y;
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("line_thickness", SettingType.positiveFloat(), 1f);
    }
}
