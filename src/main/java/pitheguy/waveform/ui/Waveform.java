package pitheguy.waveform.ui;

import com.google.common.annotations.VisibleForTesting;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.config.LoopState;
import pitheguy.waveform.io.*;
import pitheguy.waveform.io.download.DownloadFailedException;
import pitheguy.waveform.io.download.YoutubeAudioGetter;
import pitheguy.waveform.io.export.ExportManager;
import pitheguy.waveform.io.microphone.MicrophoneCapture;
import pitheguy.waveform.io.microphone.MicrophoneFrameUpdater;
import pitheguy.waveform.main.Main;
import pitheguy.waveform.main.Visualizer;
import pitheguy.waveform.ui.controls.ControlsPanel;
import pitheguy.waveform.ui.dialogs.DialogManager;
import pitheguy.waveform.ui.dialogs.preferences.PreferencesDialog;
import pitheguy.waveform.ui.drawers.AudioDrawer;
import pitheguy.waveform.ui.queue.QueueKeyboardListener;
import pitheguy.waveform.ui.queue.QueueManagementPanel;
import pitheguy.waveform.ui.util.KeyBindingManager;
import pitheguy.waveform.ui.visualizer.VisualizerSelectionWindow;
import pitheguy.waveform.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Waveform extends JFrame {
    public static final String DRAG_AND_DROP_TEXT = "Drag and drop an audio file to start playing";
    public static final String LOADING_TEXT = "Loading...";
    public static final List<String> NATIVE_FORMATS = List.of(".mp3", ".wav");
    public static final List<String> CONVENTIONAL_FORMATS = List.of(
            ".mp3", ".wav", ".flac", ".aac", ".ogg", ".m4a", ".wma", ".aiff",
            ".alac", ".opus", ".ac3", ".amr", ".dsd", ".dts", ".pcm", ".ra", ".vqf",
            ".tak", ".tta", ".wv", ".caf", ".mpc", ".snd", ".au", ".gsm"
    );
    public static final Image STATIC_ICON = new ImageIcon(Waveform.class.getResource("/icon.png")).getImage();
    private static Waveform instance;
    public static int WIDTH = Main.DEFAULT_WIDTH;
    public static int HEIGHT = Main.DEFAULT_HEIGHT;
    private final JLabel imgLabel;
    public final WaveformMenuBar menuBar;
    public double duration = 0;
    public boolean hasAudio = false;
    private boolean isFullScreen = false;
    private BufferedImage image;
    public ControlsPanel controls;
    public QueueManagementPanel queuePanel;
    public FrameUpdater frameUpdater;
    public AudioDrawer audioDrawer;
    public File audioFile;
    public AudioData audioData;
    public final TrackParsingService parsingService = new TrackParsingService(this);
    public final ExportManager exportManager = new ExportManager(this);
    public final DialogManager dialogManager = new DialogManager(this);
    public final KeyBindingManager keyBindingManager = new KeyBindingManager(this);
    public final PlaybackManager playbackManager = new PlaybackManager(this);
    public final YoutubeAudioGetter audioGetter = new YoutubeAudioGetter();
    public final MicrophoneCapture microphone = new MicrophoneCapture();
    private VisualizerSelectionWindow visualizerSelectionWindow;

    public boolean isQueuePanelVisible = false;

    public Waveform(boolean visible) {
        super("Waveform");
        instance = this;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (Config.fullScreen && visible) toggleFullscreen();
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(null);
        menuBar = new WaveformMenuBar(this);
        setJMenuBar(menuBar);
        this.imgLabel = new JLabel(LOADING_TEXT, JLabel.CENTER);
        imgLabel.setSize(WIDTH, HEIGHT);
        imgLabel.setOpaque(true);
        imgLabel.setBackground(Config.backgroundColor);
        imgLabel.setForeground(Config.foregroundColor);
        imgLabel.setFont(new Font(this.imgLabel.getFont().getName(), this.imgLabel.getFont().getStyle(), 24));
        imgLabel.setLayout(null);
        add(imgLabel);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                handleResize();
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                destroy();
            }
        });
        playbackManager.addQueueChangeListener(this::handleQueueChange);
        addMouseListener(new WaveformMouseListener());
        addKeyListener(new QueueKeyboardListener(this));
        addControls();
        addQueuePanel();
        setupKeyBindings();
        startup();
        setVisible(visible);
        setTransferHandler(new AudioTransferHandler(this));
        toFront();
    }

    public void populateMenuBar() {
        menuBar.populate();
        menuBar.setVisible(!Config.hideMenuBar);
        revalidate();
        repaint();
        menuBar.updateState();
    }

    public static Waveform getInstance() {
        return instance;
    }

    private void setupKeyBindings() {
        keyBindingManager.registerKeyBinding("F11", "toggleFullscreen", this::toggleFullscreen);
        keyBindingManager.registerKeyBinding("SPACE", "togglePlayback", this::togglePlaybackIfAllowed);
        keyBindingManager.registerKeyBinding("ctrl Q", "toggleQueuePanel", this::toggleQueuePanel);
        keyBindingManager.registerKeyBinding("ctrl P", "preferences", this::openPreferences);
        keyBindingManager.registerGlobalKeyBinding("ESCAPE", "exit", this::handleExit);
        keyBindingManager.registerGlobalKeyBinding("ctrl B", "visualizerSelection", this::toggleVisualizerSelectionWindow);
        keyBindingManager.setupKeyBindings();
    }

    public void openPreferences() {
        if (!Config.disablePreferences) PreferencesDialog.showDialog(this);
    }

    private void addControls() {
        controls = new ControlsPanel(this);
        this.imgLabel.add(controls);
        hideControls();
    }

    private void addQueuePanel() {
        queuePanel = new QueueManagementPanel(this);
        queuePanel.setVisible(false);
        imgLabel.add(queuePanel);
    }

    public void play(File audioFile) throws Exception {
        play(new TrackInfo(audioFile));
    }

    public void play(TrackInfo track) throws Exception {
        initializeTrackPlayback(track);
        if (audioData == null) return;
        startVisualization();
    }

    private void startVisualization() throws Exception {
        audioDrawer = Config.visualizer.getDrawer();
        audioDrawer.setPlayingAudio(audioData);
        if (Config.playerMode) {
            BufferedImage image = audioDrawer.drawFullAudio();
            setImageToDisplay(image);
            playbackManager.playAudio(this.audioFile);
            frameUpdater = new PlayerModeFrameUpdater(this, image);
        } else {
            playbackManager.playAudio(this.audioFile);
            frameUpdater = new FrameUpdater(sec -> {
                BufferedImage drawnArray = audioDrawer.drawFrame(sec);
                if (Config.showProgress) audioDrawer.updatePlayed(drawnArray, sec, duration);
                setImageToDisplay(drawnArray);
            }, this);
        }
        frameUpdater.start();
    }

    private void initializeTrackPlayback(TrackInfo track) throws InterruptedException {
        setResizable(Config.canResize());
        audioData = parseTrack(track);
        if (audioData == null) {
            importError();
            return;
        }
        if (frameUpdater != null) frameUpdater.silentShutdown();
        queuePanel.repopulate();
        playbackManager.initializeTrackPlayback(track);
        duration = audioData.duration();
        controls.updateState();
        menuBar.updateState();
        updateTitle();
    }

    public void play(List<TrackInfo> tracks) throws Exception {
        playbackManager.play(tracks);
        controls.updateState();
    }

    public void playFiles(List<File> audioFiles) throws Exception {
        play(audioFiles.stream().map(TrackInfo::new).toList());
    }

    public void playIndex(int index) {
        playbackManager.playIndex(index);
    }

    public void forcePlayIndex(int index) {
        playbackManager.forcePlayIndex(index);
    }

    public void microphoneInput() {
        if (!microphone.isSupported()) {
            showError("Microphone Unavailable","Microphone not available.");
            return;
        }
        Config.microphoneMode = true;
        setResizable(Config.canResize());
        playbackManager.trackTitle = "Microphone";
        if (frameUpdater != null) frameUpdater.silentShutdown();
        clearQueue();
        audioDrawer = Config.visualizer.getDrawer();
        hasAudio = true;
        updateTitle();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        microphone.startCapture();
        frameUpdater = new MicrophoneFrameUpdater(sec -> {
            AudioData audioData = AudioData.fromMicrophone(microphone.getAudioData());
            audioDrawer.setPlayingAudio(audioData);
            BufferedImage frame = audioDrawer.drawFrame(0);
            setImageToDisplay(frame);
        }, this, scheduler);
        frameUpdater.start();
    }

    public void addToQueue(int index, List<TrackInfo> tracks) throws Exception {
        List<TrackInfo> filteredTracks = new ArrayList<>(tracks);
        if (filteredTracks.stream().anyMatch(playbackManager::queueContains)) {
            int choice = JOptionPane.showConfirmDialog(this, "One or more of the selected tracks are already in the queue. Do you want to add them again?", "Duplicate Tracks Selected", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.NO_OPTION) filteredTracks.removeIf(playbackManager::queueContains);
        }
        playbackManager.addToQueue(index, filteredTracks);
        controls.showQueueConfirmation();
        if (isVisible()) Util.runInBackground(() -> filteredTracks.forEach(parsingService::preparseTrack));
    }

    public void addToQueue(List<TrackInfo> tracks) throws Exception {
        addToQueue(getQueue().size(), tracks);
    }

    public void addFilesToQueue(int index, List<File> audioFiles) throws Exception {
        addToQueue(index, audioFiles.stream().map(TrackInfo::new).toList());
    }

    public void addFilesToQueue(List<File> audioFiles) throws Exception {
        addToQueue(audioFiles.stream().map(TrackInfo::new).toList());
    }

    public void clearQueue() {
        playbackManager.clearQueue();
    }

    public List<TrackInfo> getQueue() {
        return playbackManager.queue;
    }

    public void moveTrackInQueue(int fromIndex, int toIndex) {
        playbackManager.moveTrackInQueue(fromIndex, toIndex);
    }

    public void removeIndexFromQueue(int index) {
        playbackManager.removeIndexFromQueue(index);
        controls.updateState();
        menuBar.updateState();
    }

    public int queueIndex() {
        return playbackManager.queueIndex;
    }

    public int queueSize() {
        return playbackManager.queue.size();
    }

    public void updateTitle() {
        StringBuilder title = new StringBuilder("Waveform");
        if (hasAudio) {
            title.append(" - ");
            title.append(playbackManager.trackTitle);
            if (!Config.microphoneMode) {
                title.append(" (Track ").append(queueIndex() + 1).append(" of ").append(queueSize()).append(")");
                if (isPaused()) title.append(" - Paused");
            }
        }
        setTitle(title.toString());
    }

    public String getTrackTitle() {
        return playbackManager.trackTitle;
    }

    public boolean shouldExitOnFinish() {
        return !hasNextTrack() && Config.exitOnFinish;
    }

    private AudioData parseTrack(TrackInfo track) throws InterruptedException {
        try {
            File audioFile = track.audioFile();
            if (FileConverter.needsConverted(audioFile))
                this.audioFile = FileConverter.convertAudioFile(audioFile, ".wav");
            else this.audioFile = audioFile;
            hasAudio = true;
            setLoadingText();
            return parsingService.getAudioData(track);
        } catch (IOException | ExecutionException e) {
            return null;
        }
    }

    public void invalidateAudioCache() {
        parsingService.invalidateAudioCache();
    }

    public void selectFileAndProcess(boolean addToQueue) {
        pauseUntilFinished(() -> {
            File file = selectAudioFile();
            if (file != null) Util.runInBackground(() -> Util.showErrorOnException(() -> {
                if (addToQueue) addFilesToQueue(List.of(file));
                else play(file);
            }, addToQueue ? "Failed to add selected files to the queue" : "Failed to play selected audio file"));
        });
    }

    private File selectAudioFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle("Select audio file");
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return isFileSupported(f);
            }

            @Override
            public String getDescription() {
                return "Audio files";
            }
        });
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
        else return null;
    }

    public void updateColors() {
        menuBar.updateColors();
        imgLabel.setBackground(Config.backgroundColor);
        imgLabel.setForeground(Config.foregroundColor);
    }

    public void setLoadingText() {
        setText(LOADING_TEXT);
        setCursor(Cursor.getDefaultCursor());
    }

    public void setText(String text) {
        imgLabel.setText(text);
        imgLabel.setIcon(null);
        hideControls();
        repaint();
    }

    private void importError() {
        showError("Import Error", "Failed to read audio file.");
        playbackManager.removeIndexFromQueue(playbackManager.queueIndex);
    }

    private void clearLoadingText() {
        imgLabel.setText("");
    }

    @VisibleForTesting
    public String getText() {
        return imgLabel.getText();
    }

    public void setImageToDisplay(BufferedImage image) {
        if (!hasAudio) return;
        this.image = image;
        imgLabel.setIcon(new ImageIcon(image));
        if (Config.useDynamicIcon()) setIconImage(image);
        repaint();
    }

    public void startup() {
        hasAudio = false;
        playbackManager.reset();
        menuBar.updateState();
        setText(DRAG_AND_DROP_TEXT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setResizable(true);
        setIconImage(STATIC_ICON);
        updateTitle();
    }

    public void onAudioFinished() {
        if (Config.loop == LoopState.TRACk)
            Util.showErrorOnException(() -> forcePlayIndex(queueIndex()), "Failed to replay the track");
        else if (hasNextTrack()) nextTrack();
        else if (Config.loop == LoopState.ALL) Util.showErrorOnException(() -> playIndex(0), "Failed to replay the queue");
        else startup();
    }

    public void onAudioStarted() {
        hasAudio = true;
        showControls();
        clearLoadingText();
        menuBar.updateState();
        controls.updateState();
        if (Config.isSeekingEnabled()) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void switchVisualizer(Visualizer newVisualizer) {
        if (newVisualizer.isCommandLineOnly()) throw new IllegalArgumentException("Cannot switch to a command line only visualizer");
        if (Config.visualizer == newVisualizer) return;
        if (!confirmVisualizerSwitch()) return;
        if (newVisualizer.shouldShowEpilepsyWarning() && !showEpilepsyWarning()) return;
        Config.visualizer = newVisualizer;
        audioDrawer = Config.visualizer.getDrawer();
        if (audioData != null) audioDrawer.setPlayingAudio(audioData);
        if (frameUpdater != null) frameUpdater.forceUpdate();
        setResizable(audioDrawer.isResizable() || !hasAudio);
        setCursor(getCorrectCursor());
        if (!audioDrawer.usesDynamicIcon()) setIconImage(STATIC_ICON);
        menuBar.registerVisualizerSwitch(newVisualizer);
    }

    private boolean confirmVisualizerSwitch() {
        if (!Config.visualizer.isCommandLineOnly()) return true;
        return dialogManager.showConfirmDialog("command_line_visualizer", "Confirm Visualizer Switch", """
                You are about to switch off of a command line only visualizer. You won't be able to
                switch back without rerunning the program. Are you sure you want to do this?""");
    }

    private boolean showEpilepsyWarning() {
        return dialogManager.showConfirmDialog("flashing_images", "Epilepsy Warning",
                "This visualizer may contain flashing images. Do you still want to switch to it?");
    }

    public static Cursor getCorrectCursor() {
        return Config.isSeekingEnabled() ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor();
    }

    private void handleSeeking(MouseEvent e) {
        if (!hasAudio) return;
        double percent = (double) e.getX() / WIDTH;
        long clipPosition = (long) (percent * duration * 1000000);
        long oldClipPosition = playbackManager.getMicrosecondPosition();
        playbackManager.setMicrosecondPosition(clipPosition);
        if (clipPosition < oldClipPosition) {
            audioDrawer.resetPlayed(image);
            repaint();
        }
        frameUpdater.forceUpdate();
    }

    public boolean hasPreviousTrack() {
        return playbackManager.hasPreviousTrack();
    }

    public void previousTrack() {
        playbackManager.previousTrack();
    }

    public boolean hasNextTrack() {
        return playbackManager.hasNextTrack();
    }

    public void nextTrack() {
        playbackManager.nextTrack();
    }

    public void togglePlaybackIfAllowed() {
        if (!Config.hideControls) togglePlayback();
    }

    public void togglePlayback() {
        playbackManager.togglePlayback();
        controls.updateState();
        menuBar.updateState();
        updateTitle();
    }

    public boolean isPaused() {
        return playbackManager.paused;
    }

    public static boolean isFileSupported(File file) {
        return isFileSupported(file.getName());
    }

    public static boolean isFileSupported(String name) {
        if (Config.forceRead) return true;
        if (!ResourceGetter.isFfmpegAvailable()) return NATIVE_FORMATS.stream().anyMatch(name::endsWith);
        return CONVENTIONAL_FORMATS.stream().anyMatch(name::endsWith);
    }

    private void handleResize() {
        if (frameUpdater != null) frameUpdater.pause();
        WIDTH = getWidth();
        HEIGHT = getHeight();
        imgLabel.setSize(WIDTH, HEIGHT);
        imgLabel.revalidate();
        controls.reposition();
        queuePanel.reposition();
        if (frameUpdater != null) {
            frameUpdater.resume();
            frameUpdater.forceUpdate();
        }
    }

    public void toggleFullscreen() {
        if (!isResizable()) return;
        dispose();
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (!device.isFullScreenSupported()) {
            showError("Fullscreen Error", "Fullscreen is not supported on this device.");
            return;
        }
        try {
            if (isFullScreen) {
                setUndecorated(false);
                device.setFullScreenWindow(null);
            } else {
                setUndecorated(true);
                device.setFullScreenWindow(this);
            }
            isFullScreen = !isFullScreen;
        } catch (Exception e) {
            setUndecorated(false);
            device.setFullScreenWindow(null);
            showError("Fullscreen Error", "Failed to toggle fullscreen mode: " + e.getMessage());
        }
        setVisible(true);
    }

    public void toggleVisualizerSelectionWindow() {
        if (Config.disableVisualizerSelection || !hasAudio) return;
        if (isVisualizerSelectionWindowOpen()) {
            visualizerSelectionWindow.dispose();
            visualizerSelectionWindow = null;
        } else visualizerSelectionWindow = new VisualizerSelectionWindow(this);
        menuBar.updateState();
    }

    public boolean isVisualizerSelectionWindowOpen() {
        return visualizerSelectionWindow != null;
    }


    public void toggleQueuePanel() {
        if (Config.disableQueueManagement) return;
        if (isQueuePanelVisible) hideQueuePanel();
        else showQueuePanel();
        menuBar.updateState();
    }

    private void showQueuePanel() {
        if (Config.microphoneMode) return;
        queuePanel.setVisible(true);
        queuePanel.setLocation(getContentPane().getSize().width - QueueManagementPanel.WIDTH, 0);
        queuePanel.repopulate();
        queuePanel.scrollToCurrentTrack();
        revalidate();
        repaint();
        isQueuePanelVisible = true;
    }

    public void hideQueuePanel() {
        queuePanel.setVisible(false);
        revalidate();
        repaint();
        isQueuePanelVisible = false;
    }

    public void toggleControls() {
        if (controls.isVisible()) hideControls();
        else showControls();
    }

    private void hideControls() {
        controls.setVisible(false);
    }

    private void showControls() {
        if (!Config.hideControls && !Config.microphoneMode) controls.setVisible(true);
    }

    public void importFromYoutube() {
        if (!HttpUtil.checkInternetConnection()) {
            showError("No Internet Connection", "Please check your internet connection.");
            return;
        }
        if (!YoutubeAudioGetter.hasRequiredDependencies()) {
            showError("Missing Required Dependencies", "yt-dlp is required for YouTube imports.");
            return;
        }
        DialogManager.YoutubeImportInfo importInfo = dialogManager.promptForYoutubeUrl();
        if (importInfo == null) return;
        String newUrl = YoutubeAudioGetter.validateUrl(importInfo.url(), error -> showError("Invalid URL", error));
        if (newUrl == null) return;
        importUrl(newUrl, importInfo.addToQueue());
    }

    public void importUrl(String url, boolean addToQueue) {
        boolean playlist = url.startsWith(YoutubeAudioGetter.PLAYLIST_PREFIX);
        Util.runInBackground(() -> {
            try {
                if (!addToQueue) {
                    if (frameUpdater != null) frameUpdater.silentShutdown();
                    startup();
                }
                List<TrackInfo> audioFiles = audioGetter.getAudio(url, addToQueue ? status -> controls.showText(status, ControlsPanel.NEVER_TIMEOUT) : this::setText);
                Util.logExceptions(() -> {
                    if (addToQueue) addToQueue(audioFiles);
                    else play(audioFiles);
                });
            } catch (IOException | DownloadFailedException | InterruptedException e) {
                showError("Import Failed", "Failed to import " + (playlist ? "playlist" : "audio"));
                if (!addToQueue) startup();
            }
        });
    }

    public void showError(String title, String message) {
        if (isVisible())
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        else Util.printError(message);
    }

    public void pauseUntilFinished(Runnable task) {
        boolean wasPaused = isPaused();
        if (!wasPaused && Config.pauseOnExport) togglePlayback();
        task.run();
        if (!wasPaused && Config.pauseOnExport) togglePlayback();
    }

    public void backgroundPauseUntilFinished(Runnable task, boolean waitUntilFinished) {
        boolean wasPaused = isPaused();
        if (!wasPaused && Config.pauseOnExport) togglePlayback();
        Util.runInBackground(() -> {
            task.run();
            if (!wasPaused && Config.pauseOnExport) togglePlayback();
        }, waitUntilFinished);
    }

    public void handleExit() {
        if (isQueuePanelVisible) toggleQueuePanel();
        else if (isVisualizerSelectionWindowOpen()) toggleVisualizerSelectionWindow();
        else exit();
    }

    public void exit() {
        destroy();
        System.exit(0);
    }

    private void handleQueueChange() {
        queuePanel.repopulate();
        updateTitle();
        if (getQueue().isEmpty()) {
            if (frameUpdater != null) frameUpdater.silentShutdown();
            startup();
            hideQueuePanel();
        }
    }

    public void destroy() {
        if (frameUpdater != null) frameUpdater.silentShutdown();
        playbackManager.closeAudioPlayer();
        parsingService.shutdown();
        TempFileManager.cleanupTempFiles();
        dispose();
    }

    public class WaveformMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (!hasAudio) selectFileAndProcess(false);
            else if (Config.isSeekingEnabled()) handleSeeking(e);
        }
    }
}
