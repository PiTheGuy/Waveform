package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.drawers.spectogram.ChromagramDrawer;
import pitheguy.waveform.util.FftAnalyser;

import java.awt.image.BufferedImage;

public class SelfSimilarityDrawer extends HeatmapDrawer {
    public SelfSimilarityDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    
    @Override
    protected BufferedImage precomputeImage() {
        short[] monoData = playingAudio.getMonoData();
        double[][] chromaDataWidth = new double[Waveform.WIDTH][];
        double[][] chromaDataHeight = new double[Waveform.HEIGHT][];
        double[][] frequencyDataWidth = FftAnalyser.getFrequencyData(monoData, Waveform.WIDTH);
        double[][] frequencyDataHeight = FftAnalyser.getFrequencyData(monoData, Waveform.HEIGHT);
        for (int x = 0; x < Waveform.WIDTH; x++)
            chromaDataWidth[x] = ChromagramDrawer.getChromaData(frequencyDataWidth[x], playingAudio.sampleRate());
        for (int y = 0; y < Waveform.HEIGHT; y++)
            chromaDataHeight[y] = ChromagramDrawer.getChromaData(frequencyDataHeight[y], playingAudio.sampleRate());
        double[][] similarityMatrix = new double[Waveform.WIDTH][Waveform.HEIGHT];
        for (int i = 0; i < Waveform.WIDTH; i++) {
            for (int j = 0; j < Waveform.HEIGHT; j++) {
                double similarity = cosineSimilarity(chromaDataWidth[i], chromaDataHeight[j]);
                similarityMatrix[i][j] = similarity;
            }
        }
        double[][] normalizedMatrix = new double[Waveform.WIDTH][Waveform.HEIGHT];
        int power = Config.highContrast ? 4 : 2;
        for (int x = 0; x < Waveform.WIDTH; x++)
            for (int y = 0; y < Waveform.HEIGHT; y++) normalizedMatrix[x][y] = Math.pow((similarityMatrix[x][y] + 1) / 2, power);
        return drawData(normalizedMatrix);
    }

    private double cosineSimilarity(double[] data1, double[] data2) {
        if (data1.length != data2.length) throw new IllegalArgumentException("data length does not match");
        double dotProduct = 0;
        double magnitudeA = 0;
        double magnitudeB = 0;
        for (int i = 0; i < data1.length; i++) {
            dotProduct += data1[i] * data2[i];
            magnitudeA += data1[i] * data1[i];
            magnitudeB += data2[i] * data2[i];
        }
        magnitudeA = Math.sqrt(magnitudeA);
        magnitudeB = Math.sqrt(magnitudeB);
        return dotProduct / (magnitudeA * magnitudeB);
    }


}
