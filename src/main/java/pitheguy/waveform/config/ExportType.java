package pitheguy.waveform.config;

import java.util.Arrays;

public enum ExportType {
    IMAGE(".png", "exportImage"),
    VIDEO(".mp4", "exportVideo"),
    GIF(".gif", "exportGif"),
    AUDIO(".wav", "exportAudio"),;

    private final String extension;
    private final String commandLineOption;

    ExportType(String extension, String commandLineOption) {
        this.extension = extension;
        this.commandLineOption = commandLineOption;
    }

    public String getExtension() {
        return extension;
    }

    public String getCommandLineOption() {
        return commandLineOption;
    }

    public static String[] commandLineOptions() {
        return Arrays.stream(values()).map(ExportType::getCommandLineOption).toArray(String[]::new);
    }
}
