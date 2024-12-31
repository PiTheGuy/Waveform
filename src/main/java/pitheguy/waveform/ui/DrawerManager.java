package pitheguy.waveform.ui;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.ui.drawers.AudioDrawer;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Consumer;

public class DrawerManager {
    private final Waveform parent;
    public AudioDrawer mainDrawer;
    private final Map<AudioDrawer, Consumer<BufferedImage>> auxiliaryDrawers = new HashMap<>();

    public DrawerManager(Waveform parent) {
        this.parent = parent;
    }

    public void registerAuxiliaryDrawer(AudioDrawer drawer, Consumer<BufferedImage> output) {
        drawer.setPlayingAudio(parent.audioData);
        auxiliaryDrawers.put(drawer, output);
    }

    public void unregisterAuxiliaryDrawer(AudioDrawer drawer) {
        auxiliaryDrawers.remove(drawer);
    }

    public void updateDrawers(double sec) {
        updateMainDrawer(sec);
        auxiliaryDrawers.forEach((drawer, output) -> output.accept(drawer.drawFrame(sec)));
    }

    private void updateMainDrawer(double sec) {
        BufferedImage drawnArray = mainDrawer.drawFrame(sec);
        if (Config.showProgress) mainDrawer.updatePlayed(drawnArray, sec, parent.duration);
        parent.setImageToDisplay(drawnArray);
    }

    public void setPlayingAudio(AudioData playingAudio) {
        mainDrawer.setPlayingAudio(playingAudio);
        auxiliaryDrawers.keySet().forEach(drawer -> drawer.setPlayingAudio(playingAudio));
    }

    public void regenerateIfNeeded() {
        mainDrawer.regenerateIfNeeded();
        auxiliaryDrawers.keySet().forEach(AudioDrawer::regenerateIfNeeded);
    }
}
