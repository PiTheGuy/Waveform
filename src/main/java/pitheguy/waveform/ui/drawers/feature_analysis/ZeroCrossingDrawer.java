package pitheguy.waveform.ui.drawers.feature_analysis;

import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.io.DrawContext;
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
        int[] topData = getZeroCrossingData(playingAudio.left(), context.getWidth());
        int[] bottomData = getZeroCrossingData(playingAudio.right(), context.getWidth());
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
        int mappedValue = (int) (value / maxValue * context.getHeight());
        return Math.min(mappedValue, context.getHeight());
    }

    @Override
    public SettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("relative", "Relative scaling", SettingType.BOOLEAN, false);
    }
}
