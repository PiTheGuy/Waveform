package pitheguy.waveform.util;

import pitheguy.waveform.ui.Waveform;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class ResourceGetter {
    public static String getResourcePath(String resource) {
        try {
            URL url = Waveform.class.getResource(resource);
            return Paths.get(url.toURI()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFFmpegPath() {
        if (OS.isWindows()) return getResourcePath("/ffmpeg.exe");
        else return "ffmpeg";
    }

    public static boolean isFfmpegAvailable() {
        return isDependencyAvailable("ffmpeg", "-version");
    }

    public static String getYtdlpPath() {
        if (OS.isWindows()) return getResourcePath("/yt-dlp.exe");
        else return "yt-dlp";
    }

    public static boolean isYtdlpAvailable() {
        return isDependencyAvailable("yt-dlp", "--version");
    }

    private static boolean isDependencyAvailable(String dependency, String versionArg) {
        if (OS.isWindows()) return true;
        try {
            Process process = new ProcessBuilder(dependency, versionArg).start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

}
