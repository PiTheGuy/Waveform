package pitheguy.waveform.ui;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class PlayerModeFrameUpdater extends FrameUpdater {
    private BufferedImage image;

    public PlayerModeFrameUpdater(Waveform parent, BufferedImage image) {
        super(null, parent);
        this.image = image;
    }

    @Override
    protected Consumer<Double> getTask() {
        return sec -> {
            parent.drawerManager.mainDrawer.updatePlayed(image, sec, parent.duration);
            parent.repaint();
        };
    }

    @Override
    protected void onForceUpdate() {
        image = parent.drawerManager.mainDrawer.drawFullAudio();
        parent.setImageToDisplay(image);
    }
}
