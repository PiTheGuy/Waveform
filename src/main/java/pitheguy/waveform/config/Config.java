package pitheguy.waveform.config;

import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.main.Visualizer;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.preferences.CommandLinePreferences;

import java.awt.*;
import java.io.File;

public class Config {
    public static boolean debug = false;
    public static double frameRate = 20;
    public static boolean playerMode = false;
    public static boolean silentMode = false;
    public static boolean showProgress = false;
    public static boolean exitOnFinish = false;
    public static boolean fullScreen = false;
    public static File exportFile = null;
    public static ExportType exportType = null;
    public static Visualizer visualizer = Visualizer.WAVEFORM;
    public static LoopState loop = LoopState.OFF;
    public static boolean microphoneMode = false;
    public static boolean shuffle = false;
    public static boolean disableSeeking = false;
    public static boolean disableVisualizerSelection = false;
    public static boolean disableExports = false;
    public static boolean disableUserImports = false;
    public static boolean disableSkipping = false;
    public static boolean disableQueueManagement = false;
    public static boolean disablePreferences = false;
    public static boolean hideControls = false;
    public static boolean hideMenuBar = false;
    public static CommandLinePreferences commandLinePreferences = CommandLinePreferences.EMPTY;
    public static SettingsInstance settings = SavedPreferences.createDefaultSettings();

    public static Color foregroundColor() {
        return settings.getValue("foregroundColor", Color.class);
    }

    public static Color backgroundColor() {
        return settings.getValue("backgroundColor", Color.class);
    }

    public static Color playedColor() {
        return settings.getValue("playedColor", Color.class);
    }

    public static boolean dynamicIcon() {
        return settings.getValue("dynamicIcon", Boolean.class);
    }

    public static boolean highContrast() {
        return settings.getValue("highContrast", Boolean.class);
    }

    public static boolean pauseOnExport() {
        return settings.getValue("pauseOnExport", Boolean.class);
    }

    public static NotificationState notifications() {
        return settings.getValue("notifications", NotificationState.class);
    }

    public static boolean mono() {
        return settings.getValue("mono", Boolean.class);
    }

    public static boolean disableSmoothing() {
        return settings.getValue("disableSmoothing", Boolean.class);
    }

    public static boolean showInSystemTray() {
        return settings.getValue("showInSystemTray", Boolean.class);
    }

    public static boolean forceRead() {
        return settings.getValue("forceRead", Boolean.class);
    }

    public static double getFrameLength() {
        return 1 / frameRate;
    }

    public static boolean isSeekingEnabled() {
        if (microphoneMode) return false;
        if (disableSeeking) return false;
        if (debug) return true;
        return visualizer.getDrawer().isSeekingAllowed();
    }

    public static boolean canResize() {
        return visualizer.getDrawer().isResizable();
    }

    public static boolean useDynamicIcon() {
        return Config.settings.getValue("dynamicIcon", Boolean.class) && Config.visualizer.getDrawer().usesDynamicIcon();
    }

    public static void setSettings(SettingsInstance settings) {
        boolean oldMono = settings.getValue("mono", Boolean.class);
        boolean oldShowInSystemTray = settings.getValue("showInSystemTray", Boolean.class);
        Config.settings = settings;
        if (oldMono != settings.getValue("mono", Boolean.class)) Waveform.getInstance().invalidateAudioCache();
        Boolean showInSystemTray = settings.getValue("showInSystemTray", Boolean.class);
        if (oldShowInSystemTray != showInSystemTray) {
            if (showInSystemTray) Waveform.getInstance().addTrayIcon();
            else Waveform.getInstance().removeTrayIcon();
        }
    }

}
