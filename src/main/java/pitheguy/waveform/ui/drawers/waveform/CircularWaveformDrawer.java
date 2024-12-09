package pitheguy.waveform.ui.drawers.waveform;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.drawers.CircularDrawer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CircularWaveformDrawer extends CircularDrawer {
    public short maxValue = Short.MAX_VALUE;

    public CircularWaveformDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    private double[] normalize(short[] input) {
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++)
            output[i] = (double) Math.abs(input[i]) / maxValue;
        return output;
    }

    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        super.drawAudio(sec, length);
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        g.setColor(Config.foregroundColor);
        short[] data = AudioData.averageChannels(left, right);
        drawData(g, normalize(data));
        g.dispose();
        return image;
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        updateMaxValue();
    }

    protected void updateMaxValue() {
        short[] monoData = playingAudio.getMonoData();
        short maxValue = monoData[0];
        for (int i = 1; i < monoData.length; i++) maxValue = (short) Math.max(maxValue, monoData[i]);
        this.maxValue = maxValue;
    }
}
