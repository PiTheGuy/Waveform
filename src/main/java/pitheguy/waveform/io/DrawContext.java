package pitheguy.waveform.io;

import pitheguy.waveform.ui.Waveform;

import java.util.function.IntSupplier;

public class DrawContext {
    private final IntSupplier imageWidth;
    private final IntSupplier imageHeight;
    private int cachedWidth;
    private int cachedHeight;

    private DrawContext(IntSupplier imageWidth, IntSupplier imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        updateDimensions();
    }

    private DrawContext(int imageWidth, int imageHeight) {
        this(() -> imageWidth, () -> imageHeight);
    }

    public static DrawContext realtime() {
        return new DrawContext(Waveform::getImageWidth, Waveform::getImageHeight);
    }

    public static DrawContext forExport(int width, int height) {
        return new DrawContext(width, height);
    }

    public void updateDimensions() {
        cachedWidth = imageWidth.getAsInt();
        cachedHeight = imageHeight.getAsInt();
    }

    public int getWidth() {
        return cachedWidth;
    }

    public int getHeight() {
        return cachedHeight;
    }
}
