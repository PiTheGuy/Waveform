package pitheguy.waveform.main;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.cli.*;
import pitheguy.waveform.config.*;
import pitheguy.waveform.io.TrackInfo;
import pitheguy.waveform.io.session.Session;
import pitheguy.waveform.main.validator.CommandLineValidator;
import pitheguy.waveform.main.validator.ValidationRule;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.preferences.ForcedPreferences;
import pitheguy.waveform.util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

public class Main {
    public static final int DEFAULT_WIDTH = 600;
    public static final int DEFAULT_HEIGHT = 400;
    public static final int DEFAULT_FRAME_RATE = 20;
    public static final String DEFAULT_BACKGROUND_COLOR = "black";
    public static final String DEFAULT_FOREGROUND_COLOR = "white";
    public static final String DEFAULT_PLAYED_COLOR = "red";
    public static File INPUT_FILE;
    public static String IMPORT_URL;
    private static boolean visualizerSpecified;

    public static void main(String... args) throws Exception {
        try {
            processInput(args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            new HelpFormatter().printHelp("java -jar Waveform.jar", createOptions().getVisibleOptions());
            System.exit(1);
        }
        Waveform waveform = new Waveform(Config.exportFile == null);
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        waveform.populateMenuBar();
        restoreSession();
        if (Config.exportFile != null) export(waveform);
        else if (hasInput()) playInput(waveform);
    }

    private static boolean hasInput() {
        return INPUT_FILE != null || IMPORT_URL != null || Config.microphoneMode;
    }

    private static void playInput(Waveform waveform) throws Exception {
        if (IMPORT_URL != null) waveform.importUrl(IMPORT_URL, false);
        if (INPUT_FILE != null) {
            ArrayList<File> files = new ArrayList<>(getInputFiles());
            if (files.isEmpty()) Util.printError("No valid audio files found");
            if (Config.shuffle) Collections.shuffle(files);
            waveform.playFiles(files);
        } else if (Config.microphoneMode) {
            waveform.microphoneInput();
        }
    }

    private static void export(Waveform waveform) throws Exception {
        if (Config.exportType == null) return;
        List<TrackInfo> inputTracks = getInputFilesForExport(waveform);
        if (inputTracks.isEmpty()) Util.printError("No valid audio files found");
        if (inputTracks.size() == 1) {
            waveform.play(inputTracks.getFirst());
            doExport(waveform, Config.exportFile);
        } else for (int i = 0; i < inputTracks.size(); i++) {
            TrackInfo trackInfo = inputTracks.get(i);
            String trackTitle = trackInfo.title();
            System.out.println("Exporting " + trackTitle + " (" + (i + 1) + "/" + inputTracks.size() + ")");
            waveform.play(trackInfo.audioFile());
            String name = trackTitle.contains(".") ? trackTitle.substring(0, trackTitle.lastIndexOf('.')) : trackTitle;
            File outputFile = Config.exportFile.toPath().resolve(name + ".png").toFile();
            doExport(waveform, outputFile);
        }
        waveform.exit();
    }

    private static void doExport(Waveform waveform, File outputFile) {
        switch (Config.exportType) {
            case IMAGE -> waveform.exportManager.exportFullImage(outputFile);
            case VIDEO -> waveform.exportManager.exportVideo(outputFile, true);
            case GIF -> waveform.exportManager.exportGif(outputFile, true);
            case AUDIO -> waveform.exportManager.exportAudio(outputFile, true);
        }
    }

    private static void processExportParameters(CommandLine commandLine) {
        if (commandLine.hasOption("exportImage")) {
            Config.exportFile = new File(commandLine.getOptionValue("exportImage"));
            Config.exportType = ExportType.IMAGE;
        }
        if (commandLine.hasOption("exportVideo")) {
            Config.exportFile = new File(commandLine.getOptionValue("exportVideo"));
            Config.exportType = ExportType.VIDEO;
        }
        if (commandLine.hasOption("exportGif")) {
            Config.exportFile = new File(commandLine.getOptionValue("exportGif"));
            Config.exportType = ExportType.GIF;
        }
        if (commandLine.hasOption("exportAudio")) {
            Config.exportFile = new File(commandLine.getOptionValue("exportAudio"));
            Config.exportType = ExportType.AUDIO;
        }
    }

    private static List<File> getInputFiles() {
        if (INPUT_FILE == null) return List.of();
        if (INPUT_FILE.isDirectory()) return Util.getAllFiles(INPUT_FILE).stream().filter(Waveform::isFileSupported).toList();
        else return List.of(INPUT_FILE);
    }

    private static List<TrackInfo> getInputFilesForExport(Waveform waveform) throws Exception {
        if (INPUT_FILE != null) return getInputFiles().stream().map(TrackInfo::new).toList();
        else if (IMPORT_URL != null)
            return waveform.audioGetter.getAudio(IMPORT_URL, System.out::println);
        else return List.of();
    }

    public static void processInput(String... args) throws ParseException {
        CommandLineOptions options = createOptions();
        CommandLine commandLine = new DefaultParser().parse(options.getAllOptions(), args);
        if (commandLine.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar Waveform.jar", options.getVisibleOptions());
            System.exit(0);
        }
        if (commandLine.hasOption("input")) INPUT_FILE = new File(commandLine.getOptionValue("input"));
        if (commandLine.hasOption("url")) {
            String url = commandLine.getOptionValue("url");
            Waveform.validateImport(url, Util::printError);
            IMPORT_URL = url;
        }
        if (commandLine.hasOption("fullScreen")) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Waveform.WIDTH = screenSize.width;
            Waveform.HEIGHT = screenSize.height;
        } else if (commandLine.hasOption("size")) {
            String[] size = commandLine.getOptionValues("size");
            Waveform.WIDTH = Integer.parseInt(size[0]);
            Waveform.HEIGHT = Integer.parseInt(size[1]);
        } else {
            Waveform.WIDTH = DEFAULT_WIDTH;
            Waveform.HEIGHT = DEFAULT_HEIGHT;
        }
        processExportParameters(commandLine);
        Config.debug = commandLine.hasOption("debug");
        Config.frameRate = Double.parseDouble(commandLine.getOptionValue("targetFrameRate", String.valueOf(DEFAULT_FRAME_RATE)));
        Config.playerMode = commandLine.hasOption("player");
        Config.silentMode = commandLine.hasOption("silent");
        Config.disableSeeking = commandLine.hasOption("disableSeeking");
        Config.disableVisualizerSelection = commandLine.hasOption("disableVisualizerSelection");
        Config.disableExports = commandLine.hasOption("disableExports");
        Config.disableUserImports = commandLine.hasOption("disableUserImports");
        Config.disableSkipping = commandLine.hasOption("disableSkipping");
        Config.disableDynamicIcon = commandLine.hasOption("dynamicIcon");
        Config.disablePreferences = commandLine.hasOption("disablePreferences");
        Config.hideControls = commandLine.hasOption("hideControls");
        Config.hideMenuBar = commandLine.hasOption("hideMenuBar");
        Config.showProgress = commandLine.hasOption("showProgress");
        Config.mono = commandLine.hasOption("mono");
        Config.exitOnFinish = commandLine.hasOption("exitOnFinish");
        Config.loop = commandLine.hasOption("loop") ? LoopState.ALL : LoopState.OFF;
        Config.fullScreen = commandLine.hasOption("fullScreen");
        Config.forceRead = commandLine.hasOption("force");
        Config.microphoneMode = commandLine.hasOption("microphone");
        Config.shuffle = commandLine.hasOption("shuffle");
        Config.highContrast = commandLine.hasOption("highContrast");
        Config.disableSmoothing = commandLine.hasOption("raw");
        Config.backgroundColor = Util.parseColor(commandLine.getOptionValue("backgroundColor", DEFAULT_BACKGROUND_COLOR));
        Config.foregroundColor = Util.parseColor(commandLine.getOptionValue("foregroundColor", DEFAULT_FOREGROUND_COLOR));
        Config.playedColor = Util.parseColor(commandLine.getOptionValue("playedColor", DEFAULT_PLAYED_COLOR));
        Config.visualizer = Visualizer.fromKey(commandLine.getOptionValue("visualizer", "waveform"));
        visualizerSpecified = commandLine.hasOption("visualizer");
        validateParameters(commandLine);
        Config.forcedPreferences = ForcedPreferences.fromCommandLine(commandLine);
    }

    private static void restoreSession() {
        Session.load().apply(!visualizerSpecified);
        Config.forcedPreferences.apply();
    }

    @VisibleForTesting
    public static void validateParameters(CommandLine commandLine) throws ParseException {
        CommandLineValidator validator = new CommandLineValidator();
        addInputErrors(validator);
        validator.addError(Config.frameRate <= 0, "Invalid frame rate");
        validator.addError(Config.visualizer == null, "Invalid visualizer");
        validator.addError(Config.exportFile != null && !hasInput(), "You can't export nothing");
        validator.addError(Config.backgroundColor == null, "Invalid background color. Must be one of: " + WaveColor.getAvailableColors() + ", or a hex color code");
        validator.addError(Config.foregroundColor == null, "Invalid foreground color. Must be one of: " + WaveColor.getAvailableColors() + ", or a hex color code");
        validator.addError(Config.playedColor == null, "Invalid played color. Must be one of: " + WaveColor.getAvailableColors() + ", or a hex color code");
        validator.addRule(ValidationRule.createError(Config.exportFile != null).requires("microphone").message("You can't export microphone input"));
        validator.addRule(ValidationRule.createError(!hasInput()).requires("disableUserImports").message("An input must be specified when user imports are disabled"));
        validator.addRule(ValidationRule.createWarning().requires("showProgress").requires("player").message("-showProgress was ignored because player mode already shows progress"));
        validator.addRule(ValidationRule.createWarning().requires("hideControls").requires("microphone").message("-hideControls was ignored because controls are already hidden when using the microphone as input"));
        validator.addRule(ValidationRule.createWarning().requires("disableVisualizerSelection").requires("hideMenuBar").message("-disableVisualizerSelection was ignored because the entire menu bar was already hidden"));
        validator.addRule(ValidationRule.createWarning().requires("disableExports").requires("hideMenuBar").message("-disableExports was ignored because the entire menu bar was already hidden"));
        validator.addRule(ValidationRule.createWarning(!hasInput()).requires("shuffle").message("-shuffle was ignored because no input was provided"));
        validator.addRule(ValidationRule.mutuallyExclusive("input", "url", "microphone").message("You can only specify one input source"));
        validator.addRule(ValidationRule.mutuallyExclusive("exportImage", "exportVideo", "exportGif", "exportAudio").message("You can only specify one export type"));
        validator.addRule(ValidationRule.mutuallyExclusive("loop", "exitOnFinish").message("-loop and -exitOnFinish cannot be used together"));
        validator.addRule(ValidationRule.mutuallyExclusive("fullScreen", "size").message("You can't specify explicit dimensions while in full screen"));
        validator.addWarning(() -> !Util.areUnique(Config.backgroundColor, Config.foregroundColor, Config.playedColor), "Duplicate colors found. Some UI elements may be invisible.");
        validator.validate(commandLine);
    }

    private static void addInputErrors(CommandLineValidator validator) {
        if (INPUT_FILE == null) return;
        validator.addError(!INPUT_FILE.exists(), "Input file not found");
        validator.addError(!INPUT_FILE.canRead(), "Input file cannot be read");
        validator.addRule(ValidationRule.createError(!INPUT_FILE.isDirectory() && !Waveform.isFileSupported(INPUT_FILE)).disallows("allFormats").message("Not an common audio format. Use -force to force the program to attempt to read it anyway"));
        validator.addError(Config.exportFile != null && Config.exportFile.isDirectory() != INPUT_FILE.isDirectory(), "Input and output must be both folders or both files");
    }

    public static CommandLineOptions createOptions() {
        CommandLineOptions options = new CommandLineOptions();
        options.addHiddenOption("debug", "Debug mode");
        options.addOption("help", "Print this message");
        options.addOption("visualizer", true, "Which visualizer to use");
        options.addOption(Option.builder("input").hasArg().argName("file").desc("An audio file to play, or a folder containing audio files").build());
        options.addOption("url", true, "Specify a url to import from YouTube");
        options.addOption(Option.builder("size").numberOfArgs(2).desc("The width and height of the waveform. Takes 2 arguments.").build());
        options.addOption("targetFrameRate", true, "The target frame rate (Default: " + DEFAULT_FRAME_RATE + ")");
        options.addOption("player", "Enable player mode");
        options.addOption("silent", "Don't play the audio file, just show the waveform");
        options.addOption("disableSeeking", "Disable seeking in player mode or when showing progress");
        options.addOption("disableVisualizerSelection", "Prevent the user from switching the visualizer");
        options.addOption("disableExports", "Prevent the user from exporting the visualisation");
        options.addOption("disableUserImports", "Prevent the user from manually importing an audio file");
        options.addOption("disableSkipping", "Prevent the user from skipping forward and backward in the queue");
        options.addOption("disableDynamicIcon", "Use a static icon instead of dynamically updating it, may improve performance");
        options.addOption("disableQueueManagement", "Prevent the user from opening the queue management panel");
        options.addOption("disablePreferences", "Prevents the user from opening the preferences dialog");
        options.addOption("hideControls", "Hide the playback controls");
        options.addOption("hideMenuBar", "Hides the menu bar");
        options.addOption("showProgress", "Show playback progress. Also enables seeking unless -disableSeeking is used");
        options.addOption("mono", "Disable multi-channel visualization");
        options.addOption("exitOnFinish", "Exit when the audio is finished playing. Not currently supported on folders");
        options.addOption("loop", "Loop the audio file. Not compatible with -exitOnFinish");
        options.addOption("fullScreen", "Display waveform window in full screen");
        options.addOption("force", "Attempt to read all files as audio, will throw errors on non-audio files");
        options.addOption("microphone", "Use the microphone as input");
        options.addOption("shuffle", "Shuffles the input when multiple tracks are specified");
        options.addOption("raw", "Show raw data instead of using smoothing functions");
        options.addOption("highContrast", "Increases contrast on some visualizations. Setting this option from the command line prevents the user from disabling it in the preferences dialog");
        options.addOption("backgroundColor", true, "The background color of the waveform (Default: " + DEFAULT_BACKGROUND_COLOR + ")");
        options.addOption("foregroundColor", true, "The foreground color the of the waveform (Default: " + DEFAULT_FOREGROUND_COLOR + ")");
        options.addOption("playedColor", true, "The color of the played portion of the waveform (Default: " + DEFAULT_PLAYED_COLOR + ")");
        options.addOption("exportImage", true, "Export as an image to the specified .png file");
        options.addOption("exportVideo", true, "Export as a video to the specified .mp4 file");
        options.addOption("exportGif", true, "Export as a GIF to the specified .gif file");
        options.addOption("exportAudio", true, "Export the audio to the specified .wav file");
        return options;
    }

}