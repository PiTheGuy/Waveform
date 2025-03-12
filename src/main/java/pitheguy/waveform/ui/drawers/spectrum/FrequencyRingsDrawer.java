package pitheguy.waveform.ui.drawers.spectrum;

import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.drawers.AudioDrawer;
import pitheguy.waveform.ui.drawers.CircularDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class FrequencyRingsDrawer extends AudioDrawer {

    public FrequencyRingsDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        super.drawAudio(sec, length);
        double[] frequencyData = FftAnalyser.performFFT(Util.normalize(AudioData.averageChannels(left, right)));
        int imageSize = Math.min(context.getWidth(), context.getHeight());
        int numRings = getSetting("extend_rings", Boolean.class) ? imageSize : imageSize / 2;
        double[] magnitudes = FftAnalyser.resampleMagnitudesToBands(frequencyData, numRings);
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        List<CircularDrawer.Ring> rings = new ArrayList<>();
        for (int ringNum = 0; ringNum < magnitudes.length; ringNum++) rings.add(new CircularDrawer.Ring(ringNum, magnitudes[ringNum]));
        CircularDrawer.combineRings(rings).forEach(ring -> ring.draw(context, g));
        return image;
    }

    @Override
    public boolean shouldShowEpilepsyWarning() {
        return true;
    }

    @Override
    public SettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("extend_rings", SettingType.BOOLEAN, false);
    }
}
