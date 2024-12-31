package pitheguy.waveform.ui;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.ui.drawers.AudioDrawer;
import pitheguy.waveform.util.OS;
import pitheguy.waveform.util.ResourceGetter;

import java.awt.*;

public class IconManager {
    private static final Image STATIC_ICON = ResourceGetter.getStaticIcon();
    private static final int ICON_SIZE = OS.getIconSize();
    private final Waveform parent;
    private AudioDrawer iconDrawer;

    public IconManager(Waveform parent) {
        this.parent = parent;
    }

    public void updateIconDrawer() {
        if (!Config.useDynamicIcon()) {
            resetIcon();
            return;
        }
        if (iconDrawer != null) parent.drawerManager.unregisterAuxiliaryDrawer(iconDrawer);
        iconDrawer = Config.visualizer.getNewDrawer(ICON_SIZE, ICON_SIZE);
        parent.drawerManager.registerAuxiliaryDrawer(iconDrawer, parent::setIconImage);
    }

    public void resetIcon() {
        if (iconDrawer != null) parent.drawerManager.unregisterAuxiliaryDrawer(iconDrawer);
        parent.setIconImage(STATIC_ICON);
    }
}
