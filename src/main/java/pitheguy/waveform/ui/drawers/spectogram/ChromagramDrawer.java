package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.*;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;
import pitheguy.waveform.util.rolling.RollingList;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Arrays;

public class ChromagramDrawer extends AbstractSpectrogramDrawer {
    public static final int NUM_CHROMA_BINS = 12;
    public static final double DEFAULT_ROLLING_WINDOW = 5;

    private RollingList<double[]> visualData;
    private ArrayDeque<Short> frameData;

    public ChromagramDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    
    public BufferedImage drawFrame(double sec) {
        BufferedImage image = createBlankImage();
        short[] currentFrameData = AudioData.averageChannels(left, right);
        double rollingWindow = getSetting("window", Double.class);
        int samplesPerPixel = (int) (rollingWindow * playingAudio.sampleRate() / Waveform.WIDTH);
        for (short sample : currentFrameData) frameData.addLast(sample);
        while (frameData.size() > samplesPerPixel) {
            short[] sliceData = new short[samplesPerPixel];
            for (int i = 0; i < samplesPerPixel; i++) sliceData[i] = frameData.removeFirst();
            int minFFTSize = Waveform.HEIGHT * 2;
            sliceData = Arrays.copyOf(sliceData, minFFTSize);
            double[] magnitudes = FftAnalyser.performFFT(Util.normalize(sliceData));
            visualData.add(resample(magnitudes));
        }
        for (int x = 0; x < visualData.size(); x++) {
            double[] data = visualData.get(x);
            for (int y = 0; y < Waveform.HEIGHT; y++) {
                Color color = getColor(data[y]);
                image.setRGB(x, y, color.getRGB());
            }
        }
        return image;
    }

    @Override
    protected double[] resample(double[] data) {
        double[] chromaBins = getChromaData(data, playingAudio.sampleRate());
        double[] finalData = new double[Waveform.HEIGHT];
        double pixelsPerBin = (double) Waveform.HEIGHT / NUM_CHROMA_BINS;
        for (int i = 0; i < NUM_CHROMA_BINS; i++) {
            int start = (int) (i * pixelsPerBin);
            int end = (int) ((i + 1) * pixelsPerBin);
            for (int j = start; j < end; j++) finalData[j] = chromaBins[i];
        }
        return finalData;
    }

    public static double[] getChromaData(double[] data, double sampleRate) {
        double[] chromaBins = new double[NUM_CHROMA_BINS];
        int[] chromaWeights = new int[NUM_CHROMA_BINS];
        for (int i = 0; i < data.length - 1; i++) {
            double frequency = i * sampleRate / (double) data.length;
            int chromaBin = getChromaBin(frequency);
            chromaBins[chromaBin] += data[i];
            chromaWeights[chromaBin]++;
        }
        for (int i = 0; i < chromaBins.length; i++) chromaBins[i] /= chromaWeights[i];
        return chromaBins;
    }


    @Override
    protected void initializeDataArrays() {
        super.initializeDataArrays();
        visualData = new RollingList<>(Waveform.WIDTH);
        frameData = new ArrayDeque<>();
    }

    public static int getChromaBin(double frequency) {
        int midiNote = (int) (69 + 12 * log2(frequency / 440));
        int chromaBin = midiNote % NUM_CHROMA_BINS;
        if (chromaBin < 0) chromaBin += NUM_CHROMA_BINS;
        return chromaBin;
    }

    private static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    @Override
    public boolean isSeekingAllowed() {
        return Config.playerMode;
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("window", SettingType.positiveDouble(), DEFAULT_ROLLING_WINDOW);
    }
}
