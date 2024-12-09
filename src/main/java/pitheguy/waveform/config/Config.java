package pitheguy.waveform.config;

import pitheguy.waveform.main.Visualizer;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.preferences.ForcedPreferences;

import java.awt.*;
import java.io.File;

public class Config {
    public static boolean debug = false;
    public static double frameRate = 20;
    public static boolean playerMode = false;
    public static boolean silentMode = false;
    public static boolean showProgress = false;
    public static boolean mono = false;
    public static boolean exitOnFinish = false;
    public static boolean fullScreen = false;
    public static File exportFile = null;
    public static ExportType exportType = null;
    public static Visualizer visualizer = Visualizer.WAVEFORM;
    public static LoopState loop = LoopState.OFF;
    public static boolean forceRead = false;
    public static boolean microphoneMode = false;
    public static boolean shuffle = false;
    public static boolean highContrast = false;
    public static boolean disableSmoothing = false;
    public static Color backgroundColor = Color.BLACK;
    public static Color foregroundColor = Color.WHITE;
    public static Color playedColor = Color.RED;
    public static boolean disableSeeking = false;
    public static boolean disableVisualizerSelection = false;
    public static boolean disableExports = false;
    public static boolean disableUserImports = false;
    public static boolean disableSkipping = false;
    public static boolean disableDynamicIcon = false;
    public static boolean disableQueueManagement = false;
    public static boolean disablePreferences = false;
    public static boolean hideControls = false;
    public static boolean hideMenuBar = false;
    public static ForcedPreferences forcedPreferences = ForcedPreferences.EMPTY;
    public static boolean pauseOnExport = true;

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
        return !Config.disableDynamicIcon && Config.visualizer.getDrawer().usesDynamicIcon();
    }

    public static void setMono(boolean mono) {
        if (Config.mono != mono) {
            Config.mono = mono;
            Waveform.getInstance().invalidateAudioCache();
        }
    }

}
