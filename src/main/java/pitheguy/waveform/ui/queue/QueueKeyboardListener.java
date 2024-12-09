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
        if (!parent.isQueuePanelVisible || Config.disableSkipping) return;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER -> {
                if (parent.queuePanel.isItemSelected())
                    parent.playIndex(parent.queuePanel.getSelectedIndex());
            }
            case KeyEvent.VK_DOWN -> {
                if (parent.queuePanel.getSelectedIndex() < parent.queueSize() - 1) {
                    parent.queuePanel.setSelectedIndex(parent.queuePanel.getSelectedIndex() + 1);
                    parent.queuePanel.repopulate();
                }
            }
            case KeyEvent.VK_UP -> {
                if (parent.queuePanel.getSelectedIndex() > 0) {
                    parent.queuePanel.setSelectedIndex(parent.queuePanel.getSelectedIndex() - 1);
                    parent.queuePanel.repopulate();
                }
            }
        }
    }
}
