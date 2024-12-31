package pitheguy.waveform.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import pitheguy.waveform.ui.dialogs.AboutDialog;
import pitheguy.waveform.ui.dialogs.DialogManager;
import pitheguy.waveform.ui.queue.QueueKeyboardListener;
import pitheguy.waveform.ui.util.KeyBindingManager;
import pitheguy.waveform.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.desktop.QuitResponse;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Waveform extends JFrame {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String VERSION = "1.2.1";
    public static final String DRAG_AND_DROP_TEXT = "Drag and drop an audio file to start playing";
    public static final String LOADING_TEXT = "Loading...";
    public static final List<String> NATIVE_FORMATS = List.of(".mp3", ".wav");
    public static final List<String> CONVENTIONAL_FORMATS = List.of(
            ".mp3", ".wav", ".flac", ".aac", ".ogg", ".m4a", ".wma", ".aiff",
            ".alac", ".opus", ".ac3", ".amr", ".dsd", ".dts", ".pcm", ".ra", ".vqf",
            ".tak", ".tta", ".wv", ".caf", ".mpc", ".snd", ".au", ".gsm"
    );
    public static final Image STATIC_ICON = ResourceGetter.getStaticIcon();
    private static Waveform instance;
    public static int WIDTH = Main.DEFAULT_WIDTH;
    public static int HEIGHT = Main.DEFAULT_HEIGHT;
    public double duration = 0;
    public boolean hasAudio = false;
    private boolean isFullScreen = false;
    public FrameUpdater frameUpdater;
    public File audioFile;
    public AudioData audioData;
    public WaveformTrayIcon trayIcon;
    public final TrackParsingService parsingService = new TrackParsingService(this);
    public final IconManager iconManager = new IconManager(this);
    public final DrawerManager drawerManager = new DrawerManager(this);
    public final ExportManager exportManager = new ExportManager(this);
    public final DialogManager dialogManager = new DialogManager(this);
    public final KeyBindingManager keyBindingManager = new KeyBindingManager(this);
    public final PlaybackManager playbackManager = new PlaybackManager(this);
    public final YoutubeAudioGetter audioGetter = new YoutubeAudioGetter();
    public final MicrophoneCapture microphone = new MicrophoneCapture();
    public final GuiController controller = new GuiController(this);
    private boolean shuttingDown = false;

    public Waveform(boolean visible) {
        super("Waveform");
        instance = this;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        if (Config.fullScreen && visible) toggleFullscreen();
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(null);
        setJMenuBar(controller.getMenuBar());
        add(controller.getImgLabel());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                handleResize();
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        playbackManager.addQueueChangeListener(this::handleQueueChange);
        if (Config.showInSystemTray()) addTrayIcon();
        addMouseListener(new WaveformMouseListener());
        addKeyListener(new QueueKeyboardListener(this));
        setupKeyBindings();
        setupMenuBarIntegration();
        startup();
        setVisible(visible);
        setTransferHandler(new AudioTransferHandler(this));
        toFront();
    }

    public static int getImageWidth() {
        Waveform waveform = instance;
        if (waveform.isVisible()) return waveform.getContentPane().getWidth();
        else return WIDTH;
    }

    public static int getImageHeight() {
        Waveform waveform = instance;
        if (waveform.isVisible()) return waveform.getContentPane().getHeight();
        else return HEIGHT;
    }



    public static Waveform getInstance() {
        return instance;
    }

    private void setupKeyBindings() {
        keyBindingManager.registerKeyBinding("F11", "toggleFullscreen", this::toggleFullscreen);
        keyBindingManager.registerKeyBinding("SPACE", "togglePlayback", this::togglePlaybackIfAllowed);
        keyBindingManager.registerKeyBinding("ctrl Q", "toggleQueuePanel", controller::toggleQueuePanel);
        keyBindingManager.registerKeyBinding("ctrl P", "preferences", dialogManager::openPreferences);
        keyBindingManager.registerGlobalKeyBinding("ESCAPE", "exit", this::handleExit);
        keyBindingManager.registerGlobalKeyBinding("ctrl B", "visualizerSelection", controller::toggleVisualizerSelectionWindow);
        keyBindingManager.setupKeyBindings();
    }

    private void setupMenuBarIntegration() {
        if (!Desktop.isDesktopSupported()) return;
        Desktop desktop = Desktop.getDesktop();
        if (!Config.disablePreferences && desktop.isSupported(Desktop.Action.APP_PREFERENCES))
            desktop.setPreferencesHandler(e -> dialogManager.openPreferences());
        if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) desktop.setQuitHandler((e, response) -> exit(response));
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) desktop.setPreferencesHandler(e -> new AboutDialog(this));
    }

    public boolean isMinimized() {
        return (getExtendedState() & Frame.ICONIFIED) == Frame.ICONIFIED;
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
        iconManager.updateIconDrawer();
        drawerManager.mainDrawer = Config.visualizer.getDrawer();
        drawerManager.setPlayingAudio(audioData);
        if (Config.playerMode) {
            BufferedImage image = drawerManager.mainDrawer.drawFullAudio();
            controller.setImageToDisplay(image);
            playbackManager.playAudio(this.audioFile);
            frameUpdater = new PlayerModeFrameUpdater(this, image);
        } else {
            playbackManager.playAudio(this.audioFile);
            frameUpdater = new FrameUpdater(drawerManager::updateDrawers, this);
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
        playbackManager.initializeTrackPlayback(track);
        duration = audioData.duration();
        controller.updateState();
        updateTitle();
    }

    public void play(List<TrackInfo> tracks) throws Exception {
        playbackManager.play(tracks);
        controller.updateState();
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
            showError("Microphone Unavailable", "Microphone not available.");
            return;
        }
        Config.microphoneMode = true;
        setResizable(Config.canResize());
        playbackManager.trackTitle = "Microphone";
        if (frameUpdater != null) frameUpdater.silentShutdown();
        clearQueue();
        drawerManager.mainDrawer = Config.visualizer.getDrawer();
        hasAudio = true;
        updateTitle();
        microphone.startCapture();
        frameUpdater = new MicrophoneFrameUpdater(sec -> {
            AudioData audioData = AudioData.fromMicrophone(microphone.getAudioData());
            drawerManager.setPlayingAudio(audioData);
            drawerManager.updateDrawers(0);
        }, this);
        frameUpdater.start();
    }

    public void addToQueue(int index, List<TrackInfo> tracks) throws Exception {
        List<TrackInfo> filteredTracks = new ArrayList<>(tracks);
        if (filteredTracks.stream().anyMatch(playbackManager::queueContains)) {
            int choice = JOptionPane.showConfirmDialog(this, "One or more of the selected tracks are already in the queue. Do you want to add them again?", "Duplicate Tracks Selected", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.NO_OPTION) filteredTracks.removeIf(playbackManager::queueContains);
        }
        playbackManager.addToQueue(index, filteredTracks);
        controller.showQueueConfirmation();
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

    public void processTracks(List<TrackInfo> tracks, boolean addToQueue) {
        Util.showErrorOnException(() -> {
            if (addToQueue) addToQueue(tracks);
            else play(tracks);
        }, addToQueue ? "Failed to add selected files to the queue" : "Failed to play selected audio file", LOGGER);
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
        controller.updateState();
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
            controller.setLoadingText();
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
            if (file != null) processTracks(List.of(new TrackInfo(file)), addToQueue);
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
        controller.updateColors();
    }

    private void importError() {
        showError("Import Error", "Failed to read audio file.");
        playbackManager.removeIndexFromQueue(playbackManager.queueIndex);
    }

    public void addTrayIcon() {
        if (SystemTray.isSupported()) {
            trayIcon = new WaveformTrayIcon();
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException e) {
                trayIcon = null;
                LOGGER.warn("Failed to add tray icon", e);
            }
        } else LOGGER.warn("Attempted to add tray icon to unsupported system tray");
    }

    public void removeTrayIcon() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }

    public void showNotification(String title, String message, TrayIcon.MessageType type) {
        if (trayIcon == null) return;
        if (Config.notifications().shouldNotify()) trayIcon.displayMessage(title, message, type);
    }

    public void startup() {
        hasAudio = false;
        playbackManager.reset();
        controller.updateState();
        controller.setText(DRAG_AND_DROP_TEXT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setResizable(true);
        iconManager.resetIcon();
        updateTitle();
    }

    public void onAudioFinished() {
        if (Config.loop == LoopState.TRACk)
            Util.showErrorOnException(() -> forcePlayIndex(queueIndex()), "Failed to replay the track", LOGGER);
        else if (hasNextTrack()) {
            nextTrack();
            showNotification(getTrackTitle(), null, TrayIcon.MessageType.INFO);
        } else if (Config.loop == LoopState.ALL)
            Util.showErrorOnException(() -> playIndex(0), "Failed to replay the queue", LOGGER);
        else startup();
    }

    public void onAudioStarted() {
        hasAudio = true;
        controller.showControls();
        controller.clearText();
        controller.updateState();
        if (Config.isSeekingEnabled()) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void switchVisualizer(Visualizer newVisualizer) {
        if (newVisualizer.isCommandLineOnly())
            throw new IllegalArgumentException("Cannot switch to a command line only visualizer");
        if (Config.visualizer == newVisualizer) return;
        if (Config.visualizer.isCommandLineOnly() && !confirmVisualizerSwitch()) return;
        if (newVisualizer.shouldShowEpilepsyWarning() && !showEpilepsyWarning()) return;
        Config.visualizer = newVisualizer;
        drawerManager.mainDrawer = Config.visualizer.getDrawer();
        if (audioData != null) drawerManager.setPlayingAudio(audioData);
        if (frameUpdater != null) frameUpdater.forceUpdate();
        setResizable(drawerManager.mainDrawer.isResizable() || !hasAudio);
        setCursor(getCorrectCursor());
        if (!drawerManager.mainDrawer.usesDynamicIcon()) iconManager.resetIcon();
        else iconManager.updateIconDrawer();
        controller.getMenuBar().registerVisualizerSwitch(newVisualizer);
    }

    private boolean confirmVisualizerSwitch() {
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
        double percent = (double) e.getX() / getContentPane().getWidth();
        long clipPosition = (long) (percent * duration * 1000000);
        long oldClipPosition = playbackManager.getMicrosecondPosition();
        playbackManager.setMicrosecondPosition(clipPosition);
        if (clipPosition < oldClipPosition) {
            drawerManager.mainDrawer.resetPlayed(controller.getImage());
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
        controller.updateState();
        if (trayIcon != null) trayIcon.updateState();
        updateTitle();
    }

    public boolean isPaused() {
        return playbackManager.paused;
    }

    public static boolean isFileSupported(File file) {
        return isFileSupported(file.getName());
    }

    public static boolean isFileSupported(String name) {
        if (Config.forceRead()) return true;
        if (!ResourceGetter.isFfmpegAvailable()) return NATIVE_FORMATS.stream().anyMatch(name::endsWith);
        return CONVENTIONAL_FORMATS.stream().anyMatch(name::endsWith);
    }

    private void handleResize() {
        if (frameUpdater != null) frameUpdater.pause();
        controller.handleResize();
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

    public void importFromYoutube() {
        if (!HttpUtil.ensureInternetConnection()) return;
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
                List<TrackInfo> tracks = audioGetter.getAudio(url, addToQueue ? status -> controller.showSubtext(status, ControlsPanel.NEVER_TIMEOUT) : controller::setText);
                processTracks(tracks, addToQueue);
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
        if (!wasPaused && Config.pauseOnExport()) togglePlayback();
        task.run();
        if (!wasPaused && Config.pauseOnExport()) togglePlayback();
    }

    public void backgroundPauseUntilFinished(Runnable task, boolean waitUntilFinished) {
        boolean wasPaused = isPaused();
        if (!wasPaused && Config.pauseOnExport()) togglePlayback();
        Util.runInBackground(() -> {
            task.run();
            if (!wasPaused && Config.pauseOnExport()) togglePlayback();
        }, waitUntilFinished);
    }

    private void handleQueueChange() {
        controller.updateState();
        updateTitle();
        if (getQueue().isEmpty()) {
            if (frameUpdater != null) frameUpdater.silentShutdown();
            startup();
            controller.hideQueuePanel();
        }
    }

    public void handleExit() {
        if (!controller.closeWindows()) exit();
    }

    public boolean confirmExit() {
        if (exportManager.isExporting())
            return dialogManager.showConfirmDialog("export_in_progress", "Export In Progress", "An export is currently in progress. Are you sure you want to exit the program?");
        else return true;
    }

    public void exit() {
        exit(null);
    }

    public void exit(QuitResponse response) {
        if (!confirmExit()) {
            if (response != null) response.cancelQuit();
            return;
        }
        destroy();
        if (response != null) response.performQuit();
        System.exit(0);
    }

    public void destroy() {
        shuttingDown = true;
        if (frameUpdater != null) frameUpdater.silentShutdown();
        playbackManager.closeAudioPlayer();
        parsingService.shutdown();
        exportManager.cancelExports();
        removeTrayIcon();
        TempFileManager.cleanupTempFiles();
        dispose();
    }

    public boolean isShuttingDown() {
        return shuttingDown;
    }

    public class WaveformMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (!hasAudio) selectFileAndProcess(false);
            else if (Config.isSeekingEnabled()) handleSeeking(e);
        }
    }
}
