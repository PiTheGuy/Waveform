package pitheguy.waveform.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.config.Config;

import javax.swing.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class FrameUpdater implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(FrameUpdater.class);
    protected final Waveform parent;
    private final Consumer<Double> task;
    private final ScheduledExecutorService scheduler;
    private double sec = 0;
    private boolean forceUpdate = false;
    private boolean paused = false;

    public FrameUpdater(Consumer<Double> task, Waveform parent) {
        this.task = task;
        this.parent = parent;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    protected Consumer<Double> getTask() {
        return task;
    }

    public double getSec() {
        return parent.playbackManager.hasAudioPlayer() ? parent.playbackManager.getMicrosecondPosition() / 1000000.0 : sec;
    }

    protected boolean isClipOver() {
        double sec = getSec();
        if (parent.playbackManager.hasAudioPlayer())
            return parent.duration - sec < Config.getFrameLength() && !parent.playbackManager.isAudioPlaying() && !parent.isPaused();
        return sec > parent.duration;
    }

    public void run() {
        if (paused) return;
        try {
            if (!parent.isPaused() || forceUpdate) {
                if (forceUpdate) {
                    forceUpdate = false;
                    onForceUpdate();
                }
                if (isClipOver()) {
                    shutdown();
                    if (parent.shouldExitOnFinish()) parent.exit();
                }
                getTask().accept(getSec());
                sec += Config.getFrameLength();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to render visualization", e);
            parent.togglePlayback();
            parent.dialogManager.showRenderErrorDialog();
        }
    }

    public void forceUpdate() {
        forceUpdate = true;
    }

    protected void onForceUpdate() {
    }

    public void start() {
        parent.onAudioStarted();
        scheduler.scheduleAtFixedRate(this, 0, (long) (Config.getFrameLength() * 1000), TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        silentShutdown();
        SwingUtilities.invokeLater(parent::onAudioFinished);
    }

    public void silentShutdown() {
        paused = true; // Pause to prevent updates while shutting down
        scheduler.shutdownNow();
        parent.playbackManager.closeAudioPlayer();
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }
}
