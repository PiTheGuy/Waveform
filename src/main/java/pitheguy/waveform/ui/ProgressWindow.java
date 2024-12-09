package pitheguy.waveform.ui;

import pitheguy.waveform.util.ProgressTracker;

import javax.swing.*;
import java.awt.*;

public class ProgressWindow extends JWindow implements ProgressTracker {

    private final JProgressBar progressBar;
    private final String message;
    private final int maxValue;
    private int progress = 0;

    public ProgressWindow(Waveform parent, String message, int maxValue) {
        super(parent);
        this.message = message;
        this.maxValue = maxValue;
        setLayout(new BorderLayout());
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMaximum(this.maxValue);
        add(progressBar, BorderLayout.CENTER);
        setSize(300, 80);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void step() {
        progress++;
        progressBar.setValue(progress);
        double percent = progress * 100.0 / maxValue;
        progressBar.setString("%s (%.2f%%)".formatted(message, percent));
    }

    public void setText(String text) {
        progressBar.setString(text);
    }

    @Override
    public void finish() {
        dispose();
    }
}
