package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.util.DebugText;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class VolumeDrawer extends SmoothedAudioDrawer {

    public VolumeDrawer(DrawContext context) {
        super(context, 5);
    }

    
    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        updateAudioData(sec, length);
        double[] monoData = Util.normalize(AudioData.averageChannels(left, right));
        double rms = calculateRMS(monoData);
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        g.setColor(Config.foregroundColor);
        double displayValue = getDisplayValue(rms);
        g.fillRect(0, 0, image.getWidth(), (int) (displayValue * context.getHeight()));
        drawDebugText(g, new DebugText().add("RMS", rms).add("Displayed", displayValue), Color.RED);
        return image;
    }

    public static double calculateRMS(double[] audioData) {
        double squareAverage = Arrays.stream(audioData).map(value -> value * value).average().orElse(0);
        return Math.sqrt(squareAverage);
    }

    @Override
    public boolean usesDynamicIcon() {
        return false;
    }
}
