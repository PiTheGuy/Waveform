package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.drawers.AudioDrawer;
import pitheguy.waveform.ui.util.DebugText;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

public class SpectralSparsityDrawer extends AudioDrawer {
    public SpectralSparsityDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        super.drawAudio(sec, length);
        short[] monoData = AudioData.averageChannels(left, right);
        double[] magnitudes = FftAnalyser.performFFT(Util.normalize(monoData));
        double threshold = getSetting("threshold", Double.class);
        int activeFrequencies = (int) Arrays.stream(magnitudes).filter(v -> v >= threshold).count();
        double percentage = activeFrequencies / (double) magnitudes.length;
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        Random random = new Random();
        for (int x = 0; x < context.getWidth(); x++)
            for (int y = 0; y < context.getHeight(); y++)
                if (random.nextFloat() < percentage) image.setRGB(x, y, Config.foregroundColor().getRGB());
        drawDebugText(g, new DebugText()
                .add("Active Frequencies", activeFrequencies)
                .add("Percentage", percentage), Color.GREEN);
        return image;
    }

    @Override
    public SettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("threshold", SettingType.positiveDouble(), 1.0);
    }

    @Override
    public boolean shouldShowEpilepsyWarning() {
        return true;
    }
}
