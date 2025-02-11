package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.util.Util;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ValueFrequencyHeatmapDrawer extends HeatmapDrawer {
    public ValueFrequencyHeatmapDrawer(DrawContext context) {
        super(context);
    }

    
    @Override
    protected BufferedImage precomputeImage() {
        short[][] leftData = getSlicedAudioData(context, playingAudio.left());
        short[][] rightData = getSlicedAudioData(context, playingAudio.right());
        double[][] counts = new double[context.getWidth()][context.getHeight()];
        for (int x = 0; x < context.getWidth(); x++) {
            for (short sample : leftData[x]) counts[x][getBin(sample)]++;
            for (short sample : rightData[x]) counts[x][getBin(sample)]++;
        }
        Arrays.setAll(counts, i -> Util.normalize(counts[i]));
        return HeatmapDrawer.drawData(context, counts);
    }

    private int getBin(short sample) {
        return Math.min(Math.abs(sample) * context.getHeight() / Short.MAX_VALUE, context.getHeight() - 1);
    }
}
