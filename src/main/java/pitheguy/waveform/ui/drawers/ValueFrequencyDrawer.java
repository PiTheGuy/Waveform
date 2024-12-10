package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.Util;

import java.awt.image.BufferedImage;

public class ValueFrequencyDrawer extends BarGraphDrawer {
    public ValueFrequencyDrawer(DrawContext context) {
        super(context, false);
    }

    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        super.drawAudio(sec, length);
        double[] counts = new double[getImageWidth()];
        for (short sample : left) {
            int bin = getBin(sample);
            counts[bin]++;
        }
        for (short sample : right) counts[getBin(sample)]++;
        return drawArray(Util.normalize(counts), createBlankImage());
    }

    private int getBin(short sample) {
        return Math.min(Math.abs(sample) * getImageWidth() / Short.MAX_VALUE, getImageWidth() - 1);
    }
}
