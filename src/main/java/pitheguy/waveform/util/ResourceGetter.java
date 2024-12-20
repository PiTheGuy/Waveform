package pitheguy.waveform.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.ui.Waveform;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class ResourceGetter {
    private static final Logger LOGGER = LogManager.getLogger();

    public static String getResourcePath(String resource) {
        try {
            URL url = Waveform.class.getResource(resource);
            if (url == null) throw new IOException("Resource not found: " + resource);
            return Paths.get(url.toURI()).toString();
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Could not find resource: {}", resource, e);
            return null;
        }
    }

    public static Image getStaticIcon() {
        URL iconUrl = Waveform.class.getResource("/icon.png");
        if (iconUrl == null) {
            LOGGER.warn("Could not find static icon; using default icon");
            return null;
        }
        return new ImageIcon(iconUrl).getImage();
    }

    public static ImageIcon getUiIcon(String path) {
        URL iconUrl = Waveform.class.getResource("/icons/" + path);
        if (iconUrl == null) {
            LOGGER.warn("Could not find icon for path: {}", path);
            return null;
        }
        return new ImageIcon(iconUrl);
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
        if (OS.isWindows()) {
            String resourcePath = getResourcePath("/" + dependency + ".exe");
            if (resourcePath != null) return true;
        }
        try {
            Process process = new ProcessBuilder(dependency, versionArg).start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

}
