package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class SlicedImageDrawer extends AudioDrawer {
    protected BufferedImage precomputedImage;
    BufferedImage image;
    protected boolean initialized = false;

    public SlicedImageDrawer(DrawContext context) {
        super(context);
    }

    public BufferedImage drawFullAudio() {
        Graphics2D g = image.createGraphics();
        g.drawImage(precomputedImage, 0, 0, null);
        g.dispose();
        return image;
    }

    public BufferedImage drawFrame(double sec) {
        if (!initialized) return createBlankImage();
        double duration = playingAudio.duration();
        double percentPlayed = Math.min(sec / duration, 1);
        int maxX = (int) (percentPlayed * getImageWidth());
        Graphics2D g = image.createGraphics();
        if (maxX > 0) {
            BufferedImage subimage = precomputedImage.getSubimage(0, 0, maxX, getImageHeight(context));
            g.drawImage(subimage, 0, 0, null);
            return image;
        }
        g.dispose();
        return image;
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        initialized = false;
        super.setPlayingAudio(playingAudio);
        image = createBlankImage();
        precomputedImage = precomputeImage();
        initialized = true;
    }

    @Override
    public void resetPlayed(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        if (Config.playerMode) g.drawImage(precomputedImage, 0, 0, null);
        else {
            g.setColor(Config.backgroundColor);
            g.fillRect(0, 0, getImageWidth(), getImageHeight(context));
        }
        g.dispose();
    }

    @Override
    public void regenerateIfNeeded() {
        initialized = false;
        image = createBlankImage();
        precomputedImage = precomputeImage();
        initialized = true;
    }

    protected abstract BufferedImage precomputeImage();

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    public boolean isSeekingAllowed() {
        return true;
    }
}
