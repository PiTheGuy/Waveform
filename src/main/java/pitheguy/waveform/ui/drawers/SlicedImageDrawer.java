package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.Waveform;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class SlicedImageDrawer extends AudioDrawer {
    protected BufferedImage precomputedImage;
    BufferedImage image;
    protected boolean initialized = false;

    public SlicedImageDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
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
        int maxX = (int) (percentPlayed * Waveform.WIDTH);
        Graphics2D g = image.createGraphics();
        if (maxX > 0) {
            BufferedImage subimage = precomputedImage.getSubimage(0, 0, maxX, Waveform.HEIGHT);
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
            g.fillRect(0, 0, Waveform.WIDTH, Waveform.HEIGHT);
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
}
