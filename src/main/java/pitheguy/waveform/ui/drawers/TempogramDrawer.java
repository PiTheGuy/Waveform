package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.stream.IntStream;

public class TempogramDrawer extends HeatmapDrawer {
    public static final int MAX_BPM = 300;

    public TempogramDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    
    protected BufferedImage precomputeImage() {
        short[] monoData = playingAudio.getMonoData();
        double samplesPerPixel = (double) monoData.length / Waveform.WIDTH;
        double[][] lagData = new double[Waveform.WIDTH][];
        IntStream.range(0, Waveform.WIDTH).parallel().forEach(x -> {
            int start = (int) (x * samplesPerPixel);
            int end = (int) ((x + 5) * samplesPerPixel);
            short[] columnSampleData = Arrays.copyOfRange(monoData, start, end);
            double[] frequencyData = FftAnalyser.performFFT(Util.normalize(columnSampleData));
            lagData[x] = autocorrelation(frequencyData, (int) (playingAudio.sampleRate() * 60 / MAX_BPM));
        });
        double[][] normalizedLagData = Util.normalize(lagData);
        double[][] tempogramData = new double[Waveform.WIDTH][Waveform.HEIGHT];
        for (int i = 0; i < tempogramData.length; i++) {
            double[] columnData = resample(normalizedLagData[i]);
            System.arraycopy(columnData, 0, tempogramData[i], 0, columnData.length);
        }
        return drawData(tempogramData);
    }

    private double[] resample(double[] data) {
        if (data.length == 0) return data;
        double scale = (double) data.length / Waveform.HEIGHT;
        double[] resampled = new double[Waveform.HEIGHT];
        for (int i = 0; i < Waveform.HEIGHT; i++) {
            int index = (int) (i * scale);
            resampled[i] = data[index];
        }
        return resampled;
    }

    private static double[] autocorrelation(double[] data, int minLag) {
        int n = data.length;
        if (n < minLag) return new double[0];
        double[] autocorr = new double[n - minLag];
        for (int lag = minLag; lag < n; lag++) {
            double sum = 0.0;
            int maxIndex = n - lag;
            int i = 0;
            for (; i + 3 < maxIndex;
                 i += 4) {
                sum += data[i] * data[i + lag];
                sum += data[i + 1] * data[i + lag + 1];
                sum += data[i + 2] * data[i + lag + 2];
                sum += data[i + 3] * data[i + lag + 3];
            }
            for (; i < maxIndex; i++) {
                sum += data[i] * data[i + lag];
            }

            autocorr[lag - minLag] = sum;
        }
        return autocorr;
    }
}

