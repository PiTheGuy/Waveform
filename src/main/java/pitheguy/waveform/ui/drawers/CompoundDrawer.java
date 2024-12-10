package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;

import java.awt.image.BufferedImage;

public abstract class CompoundDrawer extends AudioDrawer {
    protected AudioDrawer drawer;

    public CompoundDrawer(DrawContext context) {
        super(context);
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        super.setPlayingAudio(playingAudio);
        updateDrawer();
    }

    @Override
    public void regenerateIfNeeded() {
        updateDrawer();
    }

    private void updateDrawer() {
        drawer = getDrawer();
        drawer.setPlayingAudio(playingAudio);
    }

    protected abstract AudioDrawer getDrawer();

    @Override
    public BufferedImage drawFrame(double sec) {
        if (drawer == null) return createBlankImage();
        return drawer.drawFrame(sec);
    }

    @Override
    public BufferedImage drawFullAudio() {
        if (drawer == null) return createBlankImage();
        return drawer.drawFullAudio();
    }

    public boolean isResizable() {
        if (drawer == null) return super.isResizable();
        return drawer.isResizable();
    }

    public boolean isSeekingAllowed() {
        if (drawer == null) return super.isSeekingAllowed();
        return drawer.isSeekingAllowed();
    }

    public boolean usesDynamicIcon() {
        if (drawer == null) return super.usesDynamicIcon();
        return drawer.usesDynamicIcon();
    }

    @Override
    public boolean shouldShowEpilepsyWarning() {
        if (drawer == null) return super.shouldShowEpilepsyWarning();
        return drawer.shouldShowEpilepsyWarning();
    }
}
