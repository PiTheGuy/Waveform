package pitheguy.waveform.ui.drawers.spectrum;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.drawers.CircularDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CircularSpectrumDrawer extends CircularDrawer {
    public CircularSpectrumDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        super.drawAudio(sec, length);
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        g.setColor(Config.foregroundColor());
        short[] data = AudioData.averageChannels(left, right);
        double[] magnitudes = FftAnalyser.performFFT(Util.normalize(data));
        double[] displayData = getSetting("normalize", Boolean.class) ? Util.normalize(magnitudes) : magnitudes;
        drawData(context, g, displayData);
        return image;
    }

    @Override
    public SettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("normalize", SettingType.BOOLEAN, true);
    }
}
