package pitheguy.waveform.ui;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.download.YoutubeAudioGetter;
import pitheguy.waveform.io.session.SessionManager;
import pitheguy.waveform.main.Visualizer;
import pitheguy.waveform.ui.util.MenuHelper;
import pitheguy.waveform.util.ResourceGetter;
import pitheguy.waveform.util.Util;
import pitheguy.waveform.util.rolling.RollingList;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class WaveformMenuBar extends JMenuBar {
    public static final int MAX_SAVED_VISUALIZERS = 3;

    private final Waveform parent;
    JMenu visualizerMenu;
    JMenu exportMenu;
    JMenu trackMenu;
    JMenuItem manageQueueItem;
    JMenuItem useMicrophoneItem;
    JMenuItem clearQueueItem;
    JMenuItem previousTrackItem;
    JMenuItem pauseItem;
    JMenuItem nextTrackItem;
    JCheckBoxMenuItem hideControlsItem;
    JMenuItem exportVideoItem;
    JMenuItem exportGifItem;
    JMenuItem exitItem;
    JMenuItem importFromYoutubeItem;
    private JMenuItem preferencesItem;

    private boolean initialized = false;
    private final List<JMenu> menus = new ArrayList<>();
    private final RollingList<Visualizer> previousVisualizers = new RollingList<>(MAX_SAVED_VISUALIZERS + 1);

    WaveformMenuBar(Waveform parent) {
        this.parent = parent;
        setBackground(Config.backgroundColor);
        setForeground(Config.foregroundColor);
        setBorder(BorderFactory.createEmptyBorder());
    }

    public void populate() {
        addFileMenu();
        addExportMenu();
        addVisualizerMenu();
        addTrackMenu();
        addHelpMenu();
        initialized = true;
    }

    private void addFileMenu() {
        JMenu fileMenu = createMenu("File", 'F');
        JMenuItem openItem = MenuHelper.createMenuItem("Open...", 'O', KeyStroke.getKeyStroke("ctrl O"), e -> parent.selectFileAndProcess(false));
        JMenuItem addToQueueItem = MenuHelper.createMenuItem("Add to Queue...", 'Q', KeyStroke.getKeyStroke("shift ctrl O"), e -> parent.selectFileAndProcess(true));
        clearQueueItem = MenuHelper.createMenuItem("Clear Queue", 'C', null, e -> parent.clearQueue());
        manageQueueItem = MenuHelper.createMenuItem("Manage Queue...", 'M', KeyStroke.getKeyStroke("ctrl Q"), e -> parent.toggleQueuePanel());
        importFromYoutubeItem = MenuHelper.createMenuItem("Import from YouTube...", 'I', null, e -> parent.importFromYoutube());
        useMicrophoneItem = MenuHelper.createMenuItem("Use Microphone", 'M', null, e -> parent.microphoneInput());
        preferencesItem = MenuHelper.createMenuItem("Preferences...", 'P', KeyStroke.getKeyStroke("ctrl P"), e -> parent.openPreferences());
        exitItem = MenuHelper.createMenuItem("Exit", 'X', KeyStroke.getKeyStroke("ESCAPE"), e -> parent.handleExit());
        if (!Config.disableUserImports) {
            fileMenu.add(openItem);
            fileMenu.add(addToQueueItem);
            fileMenu.add(clearQueueItem);
            fileMenu.add(manageQueueItem);
            fileMenu.addSeparator();
            fileMenu.add(importFromYoutubeItem);
            fileMenu.add(useMicrophoneItem);
            fileMenu.addSeparator();
        }
        fileMenu.add(preferencesItem);
        fileMenu.add(exitItem);
        add(fileMenu);
    }

    private void addExportMenu() {
        exportMenu = createMenu("Export", 'E');
        JMenuItem exportFrameItem = MenuHelper.createMenuItem("Export Frame...", 'F', KeyStroke.getKeyStroke("ctrl E"), e -> parent.exportManager.exportFrame(null));
        JMenuItem exportFullItem = MenuHelper.createMenuItem("Export Full Image...", 'I', KeyStroke.getKeyStroke("shift ctrl E"), e -> parent.exportManager.exportFullImage(null, false));
        JMenuItem exportAudioFileItem = MenuHelper.createMenuItem("Export Audio...", 'A', null, e -> parent.exportManager.exportAudio());
        exportVideoItem = MenuHelper.createMenuItem("Export Video...", 'V', null, e -> parent.exportManager.exportVideo(null, false));
        exportGifItem = MenuHelper.createMenuItem("Export GIF...", 'G', null, e -> parent.exportManager.exportGif(null, false));
        exportMenu.add(exportFrameItem);
        exportMenu.add(exportFullItem);
        exportMenu.add(exportAudioFileItem);
        exportMenu.add(exportVideoItem);
        exportMenu.add(exportGifItem);
        add(exportMenu);
    }

    private void addVisualizerMenu() {
        visualizerMenu = createMenu("Visualizer", 'V');
        repopulateVisualizerMenu();
        add(visualizerMenu);
    }

    private void addTrackMenu() {
        trackMenu = createMenu("Track", 'T');
        previousTrackItem = MenuHelper.createMenuItem("Previous Track", 'R', KeyStroke.getKeyStroke("ctrl LEFT"), e -> parent.previousTrack());
        pauseItem = MenuHelper.createMenuItem("Pause", 'P', KeyStroke.getKeyStroke("SPACE"), e -> parent.togglePlayback());
        nextTrackItem = MenuHelper.createMenuItem("Next Track", 'N', KeyStroke.getKeyStroke("ctrl RIGHT"), e -> parent.nextTrack());
        hideControlsItem = MenuHelper.createCheckBoxMenuItem("Hide On-Screen Controls", 'H', null, e -> parent.toggleControls());
        trackMenu.add(previousTrackItem);
        trackMenu.add(pauseItem);
        trackMenu.add(nextTrackItem);
        trackMenu.addSeparator();
        trackMenu.add(hideControlsItem);
        add(trackMenu);
    }

    private void addHelpMenu() {
        JMenu helpMenu = createMenu("Help", 'H');
        JMenuItem reportBugsItem = MenuHelper.createMenuItem("Report Bugs...", 'R', null, e -> reportBugs());
        helpMenu.add(reportBugsItem);
        add(helpMenu);
    }

    private static void reportBugs() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/PiTheGuy/Waveform/issues"));
        } catch (IOException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void updateState() {
        if (!initialized) return;
        clearQueueItem.setVisible(!Config.microphoneMode && parent.hasAudio);
        manageQueueItem.setVisible(!Config.microphoneMode && !Config.disableQueueManagement && parent.hasAudio);
        importFromYoutubeItem.setVisible(YoutubeAudioGetter.hasRequiredDependencies());
        useMicrophoneItem.setVisible(!Config.microphoneMode);
        preferencesItem.setVisible(!Config.disablePreferences);
        exitItem.setText(getExitItemText());
        exportMenu.setVisible(parent.hasAudio && !Config.disableExports && !Config.microphoneMode);
        exportVideoItem.setVisible(ResourceGetter.isFfmpegAvailable());
        exportGifItem.setVisible(ResourceGetter.isFfmpegAvailable());
        visualizerMenu.setVisible(parent.hasAudio && !Config.disableVisualizerSelection);
        trackMenu.setVisible(!Config.hideControls && parent.hasAudio && !Config.microphoneMode);
        previousTrackItem.setEnabled(parent.hasPreviousTrack());
        previousTrackItem.setVisible(!Config.disableSkipping);
        nextTrackItem.setEnabled(parent.hasNextTrack());
        nextTrackItem.setVisible(!Config.disableSkipping);
        pauseItem.setText(parent.isPaused() ? "Play" : "Pause");
        hideControlsItem.setState(!parent.controls.isVisible());
    }

    private void repopulateVisualizerMenu() {
        visualizerMenu.removeAll();
        if (previousVisualizers.size() > 1) {
            for (int i = previousVisualizers.size() - 2; i >= 0; i--) {
                Visualizer visualizer = previousVisualizers.get(i);
                String name = visualizer.getName();
                JMenuItem menuItem = MenuHelper.createMenuItem(name, name.charAt(0), null, e -> parent.switchVisualizer(visualizer));
                visualizerMenu.add(menuItem);
            }
            visualizerMenu.addSeparator();
        }
        JMenuItem allVisualizersItem = MenuHelper.createMenuItem("All Visualizers...", 'A', KeyStroke.getKeyStroke("ctrl B"), e -> parent.toggleVisualizerSelectionWindow());
        visualizerMenu.add(allVisualizersItem);
    }

    public void applyPreviousVisualizers(List<Visualizer> previousVisualizers) {
        this.previousVisualizers.clear();
        this.previousVisualizers.addAll(previousVisualizers);
        repopulateVisualizerMenu();
    }

    public List<Visualizer> getPreviousVisualizers() {
        return previousVisualizers.stream().toList();
    }

    public void registerVisualizerSwitch(Visualizer visualizer) {
        previousVisualizers.remove(visualizer);
        previousVisualizers.add(visualizer);
        repopulateVisualizerMenu();
        if (SessionManager.getInstance() != null) SessionManager.getInstance().savePreviousVisualizers(getPreviousVisualizers());
    }

    public void updateColors() {
        setBackground(Config.backgroundColor);
        menus.forEach(menu -> menu.setForeground(Config.foregroundColor));
    }

    private String getExitItemText() {
        if (parent.isQueuePanelVisible) return "Close Queue";
        if (parent.isVisualizerSelectionWindowOpen()) return "Close Visualizer Selection";
        return "Exit";
    }

    private JMenu createMenu(String text, char mnemonic) {
        JMenu menu = new JMenu(text);
        menu.setMnemonic(mnemonic);
        menu.setForeground(Config.foregroundColor);
        menu.setName(text);
        menus.add(menu);
        return menu;
    }
}
