package pitheguy.waveform.ui.drawers.waveform;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.SettingType;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettingsInstance;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.options.ColorChannel;
import pitheguy.waveform.ui.drawers.MappedPlotDrawer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.BitSet;

public class DualWaveformDrawer extends MappedPlotDrawer {
    private ColorChannel leftChannel;
    private ColorChannel rightChannel;

    public DualWaveformDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        super.drawAudio(sec, length);
        int width = context.getWidth();
        int height = context.getHeight();
        int[] leftMapped = mapArrayToPixelHeight(left, height);
        int[] rightMapped = mapArrayToPixelHeight(right, height);
        BitSet2D leftBitSet = getWaveformBitSet(leftMapped, width, height);
        BitSet2D rightBitSet = getWaveformBitSet(rightMapped, width, height);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Config.backgroundColor);
        g.fillRect(0, 0, width, height);
        g.dispose();
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) setPixelIfNeeded(image, leftBitSet, rightBitSet, x, y);
        return image;
    }

    private void setPixelIfNeeded(BufferedImage image, BitSet2D leftBitSet, BitSet2D rightBitSet, int x, int y) {
        boolean left = leftBitSet.get(x, y);
        boolean right = rightBitSet.get(x, y);
        if (left || right) {
            image.setRGB(x, y, getColor(left, right).getRGB());
        }
    }

    protected int[] mapArrayToPixelHeight(short[] input, int height) {
        int[] output = new int[input.length];
        for (int i = 0; i < input.length; i++)
            output[i] = (int) Math.abs(((double) input[i] / maxValue * height));
        return output;
    }

    private Color getColor(boolean left, boolean right) {
        int l = leftChannel.shift(left ? 1.0 : 0.0);
        int r = rightChannel.shift(right ? 1.0 : 0.0);
        return new Color(l | r);
    }

    private BitSet2D getWaveformBitSet(int[] data, int width, int height) {
        BitSet2D bitSet = new BitSet2D(width, height);
        int halfHeight = height / 2;
        double scale = (double) data.length / width;
        for (int x = 0; x < width; x++) {
            int i = (int) (x * scale);
            int y1 = (int) (halfHeight - (double) data[i] / 2);
            int y2 = (int) (halfHeight + (double) data[i] / 2);
            for (int y = y1; y <= y2; y++) bitSet.set(x, y);
        }
        return bitSet;
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        regenerateIfNeeded();
    }

    @Override
    public void regenerateIfNeeded() {
        leftChannel = getSetting("left", ColorChannel.class);
        rightChannel = getSetting("right", ColorChannel.class);
    }

    @Override
    public VisualizerSettingsInstance.Builder constructSettings() {
        return super.constructSettings()
                .addSetting("left", SettingType.forEnum(ColorChannel.class), ColorChannel.RED)
                .addSetting("right", SettingType.forEnum(ColorChannel.class), ColorChannel.GREEN);
    }

    private static class BitSet2D {
        private final int width;
        private final int height;
        private final BitSet data;

        public BitSet2D(int width, int height) {
            this.width = width;
            this.height = height;
            data = new BitSet(width * height);
        }

        public boolean get(int x, int y) {
            if (x < 0 || x >= width || y < 0 || y >= height) throw new IndexOutOfBoundsException();
            return data.get(y * width + x);
        }

        public void set(int x, int y) {
            if (x < 0 || x >= width || y < 0 || y >= height) throw new IndexOutOfBoundsException();
            data.set(y * width + x);
        }
    }
}
