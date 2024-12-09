package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.drawers.LineGraphDrawer;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SpectralContrastDrawer extends LineGraphDrawer {
    private static final int NUM_BANDS = 8;

    public SpectralContrastDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    
    @Override
    protected BufferedImage precomputeImage() {
        short[] monoData = playingAudio.getMonoData();
        double[][] frequencyData = FftAnalyser.getFrequencyData(monoData, Waveform.WIDTH);
        double[][] contrasts = new double[NUM_BANDS][Waveform.WIDTH];
        for (int band = 0; band < NUM_BANDS; band++) {
            double[][] bandData = getBandData(frequencyData, band);
            for (int i = 0; i < Waveform.WIDTH; i++) {
                int minIndex = getBestIndex(bandData[i], false);
                int maxIndex = getBestIndex(bandData[i], true);
                int contrast = maxIndex - minIndex + 1;
                contrasts[band][i] = contrast;
            }
        }
        for (int i = 0; i < NUM_BANDS; i++)
            for (int j = 0; j < Waveform.WIDTH; j++) contrasts[i][j] /= playingAudio.sampleRate() / NUM_BANDS;
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        for (int band = 0; band < NUM_BANDS; band++)
            drawData(g, contrasts[band], Waveform.HEIGHT / NUM_BANDS, Waveform.HEIGHT - 1 - (band * Waveform.HEIGHT / NUM_BANDS), true);
        g.dispose();
        return image;
    }

    private static int getBestIndex(double[] bandData, boolean max) {
        double best = 0;
        int bestIndex = -1;
        for (int i = 0; i < bandData.length; i++) {
            double v = bandData[i];
            if (max ? v > best : v < best) {
                best = v;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private static double[][] getBandData(double[][] frequencyData, int bandIndex) {
        int numFrequencies = frequencyData[0].length;
        int frequenciesPerBand = numFrequencies / NUM_BANDS;
        double[][] bandData = new double[Waveform.WIDTH][frequenciesPerBand];
        for (int i = 0; i < Waveform.WIDTH; i++)
            System.arraycopy(frequencyData[i], bandIndex * frequenciesPerBand, bandData[i], 0, frequenciesPerBand);
        return bandData;
    }
}
