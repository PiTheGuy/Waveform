package pitheguy.waveform.ui.drawers.waveform;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.SettingType;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;

import java.awt.*;
import java.awt.image.BufferedImage;

public class EdgeWaveformDrawer extends WaveformDrawer {
    public EdgeWaveformDrawer(DrawContext context) {
        super(context);
    }

    protected BufferedImage drawAudio(double sec, double length) {
        updateAudioData(sec, length);
        int[] leftMapped = mapArrayToPixelHeight(left);
        int[] rightMapped = mapArrayToPixelHeight(right);
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int halfHeight = context.getHeight() / 2;
        double scale = (double) left.length / context.getWidth();
        g.setColor(Config.foregroundColor);
        g.setStroke(new BasicStroke(getSetting("line_thickness", Float.class)));
        for (int x = 0; x < context.getWidth(); x++) {
            int i = (int) (x * scale);
            int prevIndex = x == 0 ? 0 : (int) ((x - 1) * scale);
            int prevLeftY = halfHeight - leftMapped[prevIndex] / 2;
            int leftY = halfHeight - leftMapped[i] / 2;
            int prevRightY = halfHeight + rightMapped[prevIndex] / 2;
            int rightY = halfHeight + rightMapped[i] / 2;
            g.drawLine(x - 1, prevLeftY, x, leftY);
            g.drawLine(x - 1, prevRightY, x, rightY);
        }
        g.dispose();
        return image;
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("line_thickness", SettingType.positiveFloat(), 1f);
    }
}
