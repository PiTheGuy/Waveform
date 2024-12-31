package pitheguy.waveform.ui.queue;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.ui.Waveform;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class QueueKeyboardListener extends KeyAdapter {
    private final Waveform parent;

    public QueueKeyboardListener(Waveform parent) {
        this.parent = parent;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!parent.controller.isQueuePanelVisible() || Config.disableSkipping) return;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER -> {
                if (parent.controller.getQueuePanel().isItemSelected())
                    parent.playIndex(parent.controller.getQueuePanel().getSelectedIndex());
            }
            case KeyEvent.VK_DOWN -> {
                if (parent.controller.getQueuePanel().getSelectedIndex() < parent.queueSize() - 1) {
                    parent.controller.getQueuePanel().setSelectedIndex(parent.controller.getQueuePanel().getSelectedIndex() + 1);
                    parent.controller.getQueuePanel().repopulate();
                }
            }
            case KeyEvent.VK_UP -> {
                if (parent.controller.getQueuePanel().getSelectedIndex() > 0) {
                    parent.controller.getQueuePanel().setSelectedIndex(parent.controller.getQueuePanel().getSelectedIndex() - 1);
                    parent.controller.getQueuePanel().repopulate();
                }
            }
        }
    }
}
