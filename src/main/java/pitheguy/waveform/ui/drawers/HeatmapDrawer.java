package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class HeatmapDrawer extends SlicedImageDrawer {
    protected short[] monoData;

    public HeatmapDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    public static Color getColor(double delta) {
        return Util.blendColor(delta, Config.backgroundColor, Config.foregroundColor);
    }

    public static Color getSecondaryColor(double delta) {
        return Util.blendColor(delta, Config.foregroundColor, Config.playedColor);
    }

    protected static BufferedImage drawData(double[][] data) {
        BufferedImage image = createBlankImage();
        for (int x = 0; x < Waveform.WIDTH; x++) {
            for (int y = 0; y < Waveform.HEIGHT; y++) {
                Color color = getColor(data[x][y]);
                image.setRGB(x, Waveform.HEIGHT - 1 - y, color.getRGB());
            }
        }
        return image;
    }

    public static short[][] getSlicedAudioData(short[] sampleData) {
        double samplesPerPixel = (double) sampleData.length / Waveform.WIDTH;
        short[][] audioData = new short[Waveform.WIDTH][];
        for (int x = 0; x < Waveform.WIDTH; x++) {
            final int start = (int) (x * samplesPerPixel);
            int sampleLength = (int) samplesPerPixel;
            short[] stripData = new short[sampleLength];
            System.arraycopy(sampleData, start, stripData, 0, Math.min(sampleLength, sampleData.length - start));
            audioData[x] = stripData;
        }
        return audioData;
    }

    private static void replaceGradientPixels(BufferedImage image, Color background, Color oldForeground, Color newForeground, int maxX, double tolerance) {
        float oldRedDiff = oldForeground.getRed() - background.getRed();
        float oldGreenDiff = oldForeground.getGreen() - background.getGreen();
        float oldBlueDiff = oldForeground.getBlue() - background.getBlue();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < maxX; x++) {
                int pixelRGB = image.getRGB(x, y);
                Color pixelColor = new Color(pixelRGB, true);
                float redRatio = (pixelColor.getRed() - background.getRed()) / oldRedDiff;
                float greenRatio = (pixelColor.getGreen() - background.getGreen()) / oldGreenDiff;
                float blueRatio = (pixelColor.getBlue() - background.getBlue()) / oldBlueDiff;
                boolean isOnGradient = Math.abs(redRatio - greenRatio) < tolerance && Math.abs(redRatio - blueRatio) < tolerance && Math.abs(greenRatio - blueRatio) < tolerance;

                if (isOnGradient) {
                    float gradientRatio = (redRatio + greenRatio + blueRatio) / 3.0f;
                    gradientRatio = Math.max(0, Math.min(1, gradientRatio));
                    int newRed = (int) (background.getRed() + gradientRatio * (newForeground.getRed() - background.getRed()));
                    int newGreen = (int) (background.getGreen() + gradientRatio * (newForeground.getGreen() - background.getGreen()));
                    int newBlue = (int) (background.getBlue() + gradientRatio * (newForeground.getBlue() - background.getBlue()));
                    Color newPixelColor = new Color(newRed, newGreen, newBlue);
                    image.setRGB(x, y, newPixelColor.getRGB());
                }
            }
        }
    }

    @Override
    public void updatePlayed(BufferedImage image, double seconds, double duration) {
        int maxX = (int) (seconds / duration * Waveform.WIDTH);
        maxX = Math.min(maxX, image.getWidth());
        HeatmapDrawer.replaceGradientPixels(image, Config.backgroundColor, Config.foregroundColor, Config.playedColor, maxX, 0.1);
    }

    protected abstract BufferedImage precomputeImage();

    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        monoData = playingAudio.getMonoData();
    }

    @Override
    public boolean usesDynamicIcon() {
        return false;
    }
}