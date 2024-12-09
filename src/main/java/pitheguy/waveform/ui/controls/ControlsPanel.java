package pitheguy.waveform.ui.controls;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.Util;

import javax.swing.*;

public class ControlsPanel extends JPanel {
    public static final int SPACING = 10;
    public static final int HEIGHT = 50;
    public static final int WIDTH = PreviousTrackButton.BUTTON_WIDTH + SPACING + PauseButton.BUTTON_WIDTH + SPACING + NextTrackButton.BUTTON_WIDTH;
    public static final int PREVIOUS_TRACK_X = 0;
    public static final int PAUSE_X = PreviousTrackButton.BUTTON_WIDTH + SPACING;
    public static final int NEXT_TRACK_X = PAUSE_X + PauseButton.BUTTON_WIDTH + SPACING;

    public static final int NEVER_TIMEOUT = 0;

    private final PreviousTrackButton previousTrackButton;
    private final PauseButton pauseButton;
    private final NextTrackButton nextTrackButton;
    private final JLabel label;

    public ControlsPanel(Waveform parent) {
        previousTrackButton = new PreviousTrackButton(parent);
        previousTrackButton.setBounds(PREVIOUS_TRACK_X, 0, PreviousTrackButton.BUTTON_WIDTH, PreviousTrackButton.BUTTON_HEIGHT);
        pauseButton = new PauseButton(parent);
        pauseButton.setBounds(PAUSE_X, 0, PauseButton.BUTTON_WIDTH, PauseButton.BUTTON_HEIGHT);
        nextTrackButton = new NextTrackButton(parent);
        nextTrackButton.setBounds(NEXT_TRACK_X, 0, NextTrackButton.BUTTON_WIDTH, NextTrackButton.BUTTON_HEIGHT);
        label = new JLabel();
        label.setBounds(0, 0, WIDTH, HEIGHT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBackground(Config.backgroundColor);
        label.setForeground(Config.foregroundColor);
        label.setVisible(false);
        add(label);
        add(previousTrackButton);
        add(pauseButton);
        add(nextTrackButton);
        setLayout(null);
        setSize(WIDTH, HEIGHT);
        setOpaque(false);
        reposition();
    }

    public void reposition() {
        int x = Waveform.WIDTH / 2 - WIDTH / 2;
        int y = (Waveform.HEIGHT * 3) / 4 - WIDTH / 2;
        setBounds(x, y, WIDTH, HEIGHT);
    }

    public void updateState() {
        previousTrackButton.updateState();
        pauseButton.updateState();
        nextTrackButton.updateState();
    }

    public void showQueueConfirmation() {
        showText("Added to queue", 3000);
    }

    public void showText(String text, int timeout) {
        Util.runInBackground(() -> {
            previousTrackButton.setVisible(false);
            pauseButton.setVisible(false);
            nextTrackButton.setVisible(false);
            label.setText(text);
            label.setVisible(true);
            if (timeout != NEVER_TIMEOUT) {
                Thread.sleep(timeout);
                clearText();
            }
        });
    }

    public void clearText() {
        previousTrackButton.setVisible(true);
        pauseButton.setVisible(true);
        nextTrackButton.setVisible(true);
        label.setVisible(false);
        updateState();
    }

}
