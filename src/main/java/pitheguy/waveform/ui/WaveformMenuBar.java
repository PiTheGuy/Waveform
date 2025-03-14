package pitheguy.waveform.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.session.SessionManager;
import pitheguy.waveform.main.Visualizer;
import pitheguy.waveform.ui.dialogs.AboutDialog;
import pitheguy.waveform.ui.util.MenuHelper;
import pitheguy.waveform.util.Util;
import pitheguy.waveform.util.rolling.RollingList;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WaveformMenuBar extends JMenuBar {
    private static final Logger LOGGER = LogManager.getLogger(WaveformMenuBar.class);
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
    JMenuItem exitItem;
    JMenuItem importFromYoutubeItem;
    private JMenuItem preferencesItem;

    private boolean initialized = false;
    private final List<JMenu> menus = new ArrayList<>();
    private final RollingList<Visualizer> previousVisualizers = new RollingList<>(MAX_SAVED_VISUALIZERS + 1);

    WaveformMenuBar(Waveform parent) {
        this.parent = parent;
        setBackground(Config.backgroundColor());
        setForeground(Config.foregroundColor());
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
        manageQueueItem = MenuHelper.createMenuItem("Manage Queue...", 'M', KeyStroke.getKeyStroke("ctrl Q"), e -> parent.controller.toggleQueuePanel());
        importFromYoutubeItem = MenuHelper.createMenuItem("Import from YouTube...", 'I', null, e -> parent.importFromYoutube());
        useMicrophoneItem = MenuHelper.createMenuItem("Use Microphone", 'M', null, e -> parent.microphoneInput());
        preferencesItem = MenuHelper.createMenuItem("Preferences...", 'P', KeyStroke.getKeyStroke("ctrl P"), e -> parent.dialogManager.openPreferences());
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
        JMenuItem exportVideoItem = MenuHelper.createMenuItem("Export Video...", 'V', null, e -> parent.exportManager.exportVideo(null, false));
        JMenuItem exportGifItem = MenuHelper.createMenuItem("Export GIF...", 'G', null, e -> parent.exportManager.exportGif(null, false));
        JMenuItem exportQueueItem = MenuHelper.createMenuItem("Export Queue...", 'F', e -> parent.exportManager.exportQueue(null));
        exportMenu.add(exportFrameItem);
        exportMenu.add(exportFullItem);
        exportMenu.add(exportAudioFileItem);
        exportMenu.add(exportVideoItem);
        exportMenu.add(exportGifItem);
        exportMenu.add(exportQueueItem);
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
        hideControlsItem = MenuHelper.createCheckBoxMenuItem("Hide On-Screen Controls", 'H', null, e -> parent.controller.toggleControls());
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
        JMenuItem aboutItem = MenuHelper.createMenuItem("About...", 'A', null, e -> new AboutDialog(parent));
        if (Desktop.isDesktopSupported()) helpMenu.add(reportBugsItem);
        helpMenu.add(aboutItem);
        add(helpMenu);
    }

    private void reportBugs() {
        Util.openUrl("https://github.com/PiTheGuy/Waveform/issues", "bug report");
    }

    public void updateState() {
        if (!initialized) return;
        clearQueueItem.setVisible(!Config.microphoneMode && parent.hasAudio);
        manageQueueItem.setVisible(!Config.microphoneMode && !Config.disableQueueManagement && parent.hasAudio);
        useMicrophoneItem.setVisible(!Config.microphoneMode);
        preferencesItem.setVisible(!Config.disablePreferences);
        exitItem.setText(getExitItemText());
        exportMenu.setVisible(parent.hasAudio && !Config.disableExports && !Config.microphoneMode);
        visualizerMenu.setVisible(parent.hasAudio && !Config.disableVisualizerSelection);
        trackMenu.setVisible(!Config.hideControls && parent.hasAudio && !Config.microphoneMode);
        previousTrackItem.setEnabled(parent.hasPreviousTrack());
        previousTrackItem.setVisible(!Config.disableSkipping);
        nextTrackItem.setEnabled(parent.hasNextTrack());
        nextTrackItem.setVisible(!Config.disableSkipping);
        pauseItem.setText(parent.isPaused() ? "Play" : "Pause");
        hideControlsItem.setState(!parent.controller.controlsVisible());
    }

    private void repopulateVisualizerMenu() {
        visualizerMenu.removeAll();
        List<Visualizer> recentVisualizers = getRecentVisualizers();
        if (!recentVisualizers.isEmpty()) {
            for (Visualizer visualizer : recentVisualizers) {
                String name = visualizer.getName();
                JMenuItem menuItem = MenuHelper.createMenuItem(name, name.charAt(0), null, e -> parent.switchVisualizer(visualizer));
                visualizerMenu.add(menuItem);
            }
            visualizerMenu.addSeparator();
        }
        JMenuItem allVisualizersItem = MenuHelper.createMenuItem("All Visualizers...", 'A', KeyStroke.getKeyStroke("ctrl B"), e -> parent.controller.toggleVisualizerSelectionWindow());
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

    public List<Visualizer> getRecentVisualizers() {
        List<Visualizer> previousVisualizers = getPreviousVisualizers();
        if (previousVisualizers.isEmpty()) return new ArrayList<>();
        List<Visualizer> recentVisualizers = previousVisualizers.reversed().subList(1, previousVisualizers.size());
        return new ArrayList<>(recentVisualizers);
    }

    public void registerVisualizerSwitch(Visualizer visualizer) {
        previousVisualizers.remove(visualizer);
        previousVisualizers.add(visualizer);
        repopulateVisualizerMenu();
        if (SessionManager.getInstance() != null) SessionManager.getInstance().savePreviousVisualizers(getPreviousVisualizers());
    }

    public void updateColors() {
        setBackground(Config.backgroundColor());
        menus.forEach(menu -> menu.setForeground(Config.foregroundColor()));
    }

    private String getExitItemText() {
        if (parent.controller.isQueuePanelVisible()) return "Close Queue";
        if (parent.controller.isVisualizerSelectionWindowOpen()) return "Close Visualizer Selection";
        return "Exit";
    }

    private JMenu createMenu(String text, char mnemonic) {
        JMenu menu = new JMenu(text);
        menu.setMnemonic(mnemonic);
        menu.setForeground(Config.foregroundColor());
        menu.setName(text);
        menus.add(menu);
        return menu;
    }
}
