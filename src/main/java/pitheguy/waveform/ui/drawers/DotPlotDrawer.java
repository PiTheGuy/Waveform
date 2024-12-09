package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class DotPlotDrawer extends MappedPlotDrawer {
    public DotPlotDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    protected void drawPoint(Graphics2D g, int x, int y) {
        int pointRadius = getSetting("point_radius", Integer.class);
        g.fillRect(x - pointRadius, y - pointRadius, pointRadius * 2 + 1, pointRadius * 2 + 1);
    }

    protected int[] mapArrayToPixelCoords(short[] input, int maxCoord) {
        int[] output = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            double normalized = (double) input[i] / maxValue;
            int newValue = (int) (normalized * (maxCoord / 2.0));
            output[i] = newValue + maxCoord / 2;
        }
        return output;
    }

    protected BufferedImage drawData(short[] xAxis, short[] yAxis) {
        BufferedImage image = createBlankImage();
        int[] mappedXAxis = mapArrayToPixelCoords(xAxis, Waveform.WIDTH);
        int[] mappedYAxis = mapArrayToPixelCoords(yAxis, Waveform.HEIGHT);
        Graphics2D g = image.createGraphics();
        g.setColor(Config.foregroundColor);
        for (int i = 0; i < xAxis.length; i++) drawPoint(g, mappedXAxis[i], mappedYAxis[i]);
        g.dispose();
        return image;
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("point_radius", SettingType.positiveInt(), 1);
    }
}
