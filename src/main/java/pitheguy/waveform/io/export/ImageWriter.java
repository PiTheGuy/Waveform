package pitheguy.waveform.io.export;

import pitheguy.waveform.ui.Waveform;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

public class ImageWriter {
    byte[] pixels;

    public ImageWriter(int width, int height) {
        pixels = new byte[width * height * 3];
    }

    public void writeImageToStream(BufferedImage image, OutputStream outputStream) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int index = (y * width + x) * 3;
                pixels[index] = (byte) ((rgb >> 16) & 0xFF);  // Red
                pixels[index + 1] = (byte) ((rgb >> 8) & 0xFF);  // Green
                pixels[index + 2] = (byte) (rgb & 0xFF);  // Blue
            }
        }
        outputStream.write(pixels);
    }
}
