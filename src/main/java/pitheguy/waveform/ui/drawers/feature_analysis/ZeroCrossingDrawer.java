package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.SettingType;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.ui.drawers.SlicedImageDrawer;
import pitheguy.waveform.ui.drawers.waveform.WaveformDrawer;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ZeroCrossingDrawer extends SlicedImageDrawer {
    public ZeroCrossingDrawer(DrawContext context) {
        super(context);
    }

    
    @Override
    protected BufferedImage precomputeImage() {
        int[] topData = getZeroCrossingData(playingAudio.left(), getImageWidth());
        int[] bottomData = getZeroCrossingData(playingAudio.right(), getImageWidth());
        return WaveformDrawer.drawData(context, createBlankImage(), topData, bottomData);
    }

    private int[] getZeroCrossingData(short[] audioData, int arraySize) {
        double samplesPerPixel = (double) audioData.length / arraySize;
        int[] zeroCrossingData = new int[arraySize];
        for (int i = 0; i < arraySize; i++) {
            int start = (int) (i * samplesPerPixel);
            int end = (int) ((i + 1) * samplesPerPixel);
            int zeroCrossings = 0;
            boolean positive = false;
            for (int j = start; j < end; j++) {
                if (audioData[j] > 0 != positive) {
                    positive = !positive;
                    zeroCrossings++;
                }
            }
            zeroCrossingData[i] = zeroCrossings;
        }
        return remapZeroCrossingData(zeroCrossingData, samplesPerPixel);
    }

    private int[] remapZeroCrossingData(int[] zeroCrossingData, double samplesPerPixel) {
        int audioMax = Arrays.stream(zeroCrossingData).max().orElseThrow();
        double maxValue = getSetting("relative", Boolean.class) ? audioMax : samplesPerPixel;
        return Arrays.stream(zeroCrossingData).map(value -> mapValue(value, maxValue)).toArray();
    }

    private int mapValue(int value, double maxValue) {
        int mappedValue = (int) (value / maxValue * getImageHeight(context));
        return Math.min(mappedValue, getImageHeight(context));
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("relative", "Relative scaling", SettingType.bool(), false);
    }
}
