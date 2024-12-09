package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.Util;

import java.awt.image.BufferedImage;

public class ValueFrequencyDrawer extends BarGraphDrawer {
    public ValueFrequencyDrawer(boolean forceFullAudio) {
        super(forceFullAudio, false);
    }

    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        super.drawAudio(sec, length);
        double[] counts = new double[Waveform.WIDTH];
        for (short sample : left) {
            int bin = getBin(sample);
            counts[bin]++;
        }
        for (short sample : right) counts[getBin(sample)]++;
        return drawArray(Util.normalize(counts), createBlankImage());
    }

    private int getBin(short sample) {
        return Math.min(Math.abs(sample) * Waveform.WIDTH / Short.MAX_VALUE, Waveform.WIDTH - 1);
    }
}
