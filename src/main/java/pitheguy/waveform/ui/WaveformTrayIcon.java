package pitheguy.waveform.ui;

import java.awt.*;

public class WaveformTrayIcon extends TrayIcon {
    public WaveformTrayIcon() {
        super(Waveform.STATIC_ICON, "Waveform", createPopupMenu());
        setImageAutoSize(true);
        addActionListener(e -> Waveform.getInstance().setExtendedState(Frame.NORMAL));
    }

    private static PopupMenu createPopupMenu() {
        PopupMenu popup = new PopupMenu();
        MenuItem openItem = new MenuItem("Open");
        openItem.addActionListener(e -> Waveform.getInstance().setExtendedState(Frame.NORMAL));
        MenuItem pauseItem = new MenuItem("Pause");
        pauseItem.addActionListener(e -> Waveform.getInstance().togglePlayback());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> Waveform.getInstance().exit());
        popup.add(openItem);
        popup.add(pauseItem);
        popup.add(exitItem);
        return popup;
    }

    public void updateState() {
        PopupMenu menu = getPopupMenu();
        MenuItem pauseItem = menu.getItem(1);
        pauseItem.setLabel(Waveform.getInstance().isPaused() ? "Play" : "Pause");
    }
}
