package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.Util;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ValueFrequencyHeatmapDrawer extends HeatmapDrawer {
    public ValueFrequencyHeatmapDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    
    @Override
    protected BufferedImage precomputeImage() {
        short[][] leftData = getSlicedAudioData(playingAudio.left());
        short[][] rightData = getSlicedAudioData(playingAudio.right());
        double[][] counts = new double[Waveform.WIDTH][Waveform.HEIGHT];
        for (int x = 0; x < Waveform.WIDTH; x++) {
            for (short sample : leftData[x]) counts[x][getBin(sample)]++;
            for (short sample : rightData[x]) counts[x][getBin(sample)]++;
        }
        Arrays.setAll(counts, i -> Util.normalize(counts[i]));
        return HeatmapDrawer.drawData(counts);
    }

    private int getBin(short sample) {
        return Math.min(Math.abs(sample) * Waveform.HEIGHT / Short.MAX_VALUE, Waveform.HEIGHT - 1);
    }
}
