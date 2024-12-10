package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.image.BufferedImage;

public class TempoDrawer extends BarGraphDrawer {
    public TempoDrawer(DrawContext context) {
        super(context, true);
    }

    
    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        updateAudioData(sec, length);
        double[] lagData = getLagData();
        return drawArray(lagData, createBlankImage());
    }

    protected double[] getLagData() {
        short[] data = AudioData.averageChannels(left, right);
        double[] magnitudes = FftAnalyser.performFFT(Util.normalize(data));
        double[] lagData = autocorrelation(magnitudes);
        return Util.normalize(lagData);
    }

    public static double[] autocorrelation(double[] data) {
        int n = data.length;
        double[] autocorr = new double[n];
        for (int lag = 0; lag < n; lag++) {
            double sum = 0.0;
            for (int i = 0; i < n - lag; i++) sum += data[i] * data[i + lag];
            autocorr[lag] = sum;
        }
        return autocorr;
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
    }

    @Override
    public boolean isSeekingAllowed() {
        return false;
    }
}
