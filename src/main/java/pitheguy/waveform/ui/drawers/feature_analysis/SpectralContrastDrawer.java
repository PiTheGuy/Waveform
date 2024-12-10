package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.drawers.LineGraphDrawer;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SpectralContrastDrawer extends LineGraphDrawer {
    private static final int NUM_BANDS = 8;

    public SpectralContrastDrawer(DrawContext context) {
        super(context);
    }

    
    @Override
    protected BufferedImage precomputeImage() {
        short[] monoData = playingAudio.getMonoData();
        double[][] frequencyData = FftAnalyser.getFrequencyData(monoData, getImageWidth());
        double[][] contrasts = new double[NUM_BANDS][getImageWidth()];
        for (int band = 0; band < NUM_BANDS; band++) {
            double[][] bandData = getBandData(frequencyData, band);
            for (int i = 0; i < getImageWidth(); i++) {
                int minIndex = getBestIndex(bandData[i], false);
                int maxIndex = getBestIndex(bandData[i], true);
                int contrast = maxIndex - minIndex + 1;
                contrasts[band][i] = contrast;
            }
        }
        for (int i = 0; i < NUM_BANDS; i++)
            for (int j = 0; j < getImageWidth(); j++) contrasts[i][j] /= playingAudio.sampleRate() / NUM_BANDS;
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        for (int band = 0; band < NUM_BANDS; band++)
            drawData(g, contrasts[band], getImageHeight(context) / NUM_BANDS, getImageHeight(context) - 1 - (band * getImageHeight(context) / NUM_BANDS), true);
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

    private double[][] getBandData(double[][] frequencyData, int bandIndex) {
        int numFrequencies = frequencyData[0].length;
        int frequenciesPerBand = numFrequencies / NUM_BANDS;
        double[][] bandData = new double[getImageWidth()][frequenciesPerBand];
        for (int i = 0; i < getImageWidth(); i++)
            System.arraycopy(frequencyData[i], bandIndex * frequenciesPerBand, bandData[i], 0, frequenciesPerBand);
        return bandData;
    }
}
