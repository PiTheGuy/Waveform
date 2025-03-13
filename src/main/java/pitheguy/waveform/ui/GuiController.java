package pitheguy.waveform.ui;

import com.google.common.annotations.VisibleForTesting;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.ui.controls.ControlsPanel;
import pitheguy.waveform.ui.queue.QueueManagementPanel;
import pitheguy.waveform.ui.visualizer.VisualizerSelectionWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GuiController {
    public static final String DRAG_AND_DROP_TEXT = "Drag and drop an audio file to start playing";
    private final Waveform parent;
    private final WaveformMenuBar menuBar;
    private VisualizerSelectionWindow visualizerSelectionWindow;
    private QueueManagementPanel queuePanel;
    private ControlsPanel controls;
    private final JLabel imgLabel;
    private JProgressBar progressBar;
    private boolean isQueuePanelVisible = false;
    private BufferedImage image;

    public GuiController(Waveform parent) {
        this.parent = parent;
        menuBar = new WaveformMenuBar(parent);
        imgLabel = new JLabel(Waveform.LOADING_TEXT, JLabel.CENTER);
        imgLabel.setOpaque(true);
        imgLabel.setBackground(Config.backgroundColor());
        imgLabel.setForeground(Config.foregroundColor());
        imgLabel.setFont(new Font(this.imgLabel.getFont().getName(), this.imgLabel.getFont().getStyle(), 24));
        imgLabel.setLayout(null);
        addControls();
        addQueuePanel();
        addProgressBar();
    }

    public void addProgressBar() {
        progressBar = new JProgressBar();
        int x = (int) (parent.getWidth() * 0.3);
        int y = (int) (parent.getHeight() * 0.6);
        int width = (int) (parent.getWidth() * 0.4);
        int height = (int) (parent.getHeight() * 0.1);
        progressBar.setBounds(x, y, width, height);
        progressBar.setBackground(Config.backgroundColor());
        progressBar.setForeground(Config.foregroundColor());
        progressBar.setVisible(false);
        progressBar.setMaximum(1000);
        parent.add(progressBar);
    }

    public void showProgressBar() {
        progressBar.setVisible(true);
        progressBar.setValue(0);
    }

    public void hideProgressBar() {
        progressBar.setVisible(false);
    }

    public void setProgress(double progress) {
        progressBar.setValue((int) (progress * 1000));
    }

    public void populateMenuBar() {
        menuBar.populate();
        menuBar.setVisible(!Config.hideMenuBar);
        parent.revalidate();
        parent.repaint();
        updateState();;
        imgLabel.setSize(parent.getContentPane().getWidth(), parent.getContentPane().getHeight()); // Needs to be done after menu population
    }

    public void toggleVisualizerSelectionWindow() {
        if (Config.disableVisualizerSelection || !parent.hasAudio) return;
        if (isVisualizerSelectionWindowOpen()) {
            visualizerSelectionWindow.dispose();
            visualizerSelectionWindow = null;
        } else visualizerSelectionWindow = new VisualizerSelectionWindow(parent, false);
        updateState();
    }

    public void showErrorVisualizerSelectionWindow() {
        if (Config.disableVisualizerSelection || !parent.hasAudio) return;
        visualizerSelectionWindow = new VisualizerSelectionWindow(parent, true);
        updateState();
    }

    public boolean isVisualizerSelectionWindowOpen() {
        return visualizerSelectionWindow != null;
    }

    public void toggleQueuePanel() {
        if (Config.disableQueueManagement) return;
        if (isQueuePanelVisible) hideQueuePanel();
        else showQueuePanel();
        updateState();
    }

    private void showQueuePanel() {
        if (Config.microphoneMode) return;
        queuePanel.setVisible(true);
        queuePanel.setLocation(parent.getContentPane().getSize().width - QueueManagementPanel.WIDTH, 0);
        queuePanel.repopulate();
        queuePanel.scrollToCurrentTrack();
        isQueuePanelVisible = true;
        parent.repaint();
    }

    public void hideQueuePanel() {
        queuePanel.setVisible(false);
        isQueuePanelVisible = false;
        parent.repaint();
    }

    public boolean isQueuePanelVisible() {
        return isQueuePanelVisible;
    }

    public QueueManagementPanel getQueuePanel() {
        return queuePanel;
    }

    public void toggleControls() {
        if (controls.isVisible()) hideControls();
        else showControls();
    }

    private void hideControls() {
        controls.setVisible(false);
    }

    public void showControls() {
        if (!Config.hideControls && !Config.microphoneMode) controls.setVisible(true);
    }

    public boolean controlsVisible() {
        return controls.isVisible();
    }

    public void showQueueConfirmation() {
        controls.showQueueConfirmation();
    }

    public void showSubtext(String text, int timeout) {
        controls.showText(text, timeout);
    }

    public void updateState() {
        menuBar.updateState();
        controls.updateState();
        queuePanel.repopulate();
    }

    public void updateColors() {
        menuBar.updateColors();
        imgLabel.setBackground(Config.backgroundColor());
        imgLabel.setForeground(Config.foregroundColor());
        progressBar.setBackground(Config.backgroundColor());
        progressBar.setForeground(Config.foregroundColor());
    }

    public JLabel getImgLabel() {
        return imgLabel;
    }

    private void addControls() {
        if (Config.hideControls) return;
        controls = new ControlsPanel(parent);
        this.imgLabel.add(controls);
        hideControls();
    }

    private void addQueuePanel() {
        if (Config.disableQueueManagement) return;
        queuePanel = new QueueManagementPanel(parent);
        queuePanel.setVisible(false);
        imgLabel.add(queuePanel);
    }

    public void setText(String text) {
        imgLabel.setText(text);
        imgLabel.setIcon(null);
        hideControls();
        parent.repaint();
    }

    public void setLoadingText() {
        setText(Waveform.LOADING_TEXT);
    }

    public void clearText() {
        imgLabel.setText("");
    }

    @VisibleForTesting
    public String getText() {
        return imgLabel.getText();
    }

    public void setImageToDisplay(BufferedImage image) {
        if (!parent.hasAudio) return;
        this.image = image;
        imgLabel.setIcon(new ImageIcon(image));
        parent.repaint();
    }

    public BufferedImage getImage() {
        return image;
    }

    public WaveformMenuBar getMenuBar() {
        return menuBar;
    }

    public void handleResize() {
        imgLabel.setSize(parent.getContentPane().getWidth(), parent.getContentPane().getHeight());
        imgLabel.revalidate();
        controls.reposition();
        queuePanel.reposition();
        int x = (int) (parent.getWidth() * 0.3);
        int y = (int) (parent.getHeight() * 0.6);
        int width = (int) (parent.getWidth() * 0.4);
        int height = (int) (parent.getHeight() * 0.1);
        progressBar.setBounds(x, y, width, height);
    }

    public void onAudioStarted() {
        showControls();
        clearText();
        updateState();
        if (Config.isSeekingEnabled()) setCursor(Cursor.HAND_CURSOR);
    }

    public void reset() {
        updateState();
        setText(DRAG_AND_DROP_TEXT);
        setCursor(Cursor.HAND_CURSOR);
    }

    public void setCursor(int cursor) {
        parent.setCursor(Cursor.getPredefinedCursor(cursor));
    }

    public boolean closeWindows() {
        if (isQueuePanelVisible) {
            toggleQueuePanel();
            return true;
        } else if (isVisualizerSelectionWindowOpen()) {
            toggleVisualizerSelectionWindow();
            return true;
        } else return false;
    }
}
