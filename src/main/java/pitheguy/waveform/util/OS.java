package pitheguy.waveform.util;

import com.sun.jna.platform.win32.User32;
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;

public enum OS {
    WINDOWS,
    MAC_OS,
    LINUX,
    OTHER;

    public static OS detectOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) return WINDOWS;
        else if (osName.contains("mac")) return MAC_OS;
        else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) return LINUX;
        else return OTHER;
    }

    public static boolean isWindows() {
        return detectOS() == WINDOWS;
    }

    public static File getSessionFile() {
        return getProgramDataPath().resolve("session.json").toFile();
    }

    public static Path getProgramDataPath() {
        OS os = detectOS();
        String filePath = switch (os) {
            case WINDOWS -> System.getenv("APPDATA") + "/Waveform";
            case MAC_OS -> System.getProperty("user.home") + "/Library/Application Support/Waveform";
            case LINUX -> System.getProperty("user.home") + "/.config/Waveform";
            default -> System.getProperty("user.home") + "/Waveform";
        };
        return Path.of(filePath);
    }

    public static int getIconSize() {
        OS os = detectOS();
        try {
            return switch (os) {
                case WINDOWS -> {
                    int iconWidth = User32.INSTANCE.GetSystemMetrics(11);
                    int iconHeight = User32.INSTANCE.GetSystemMetrics(12);
                    yield Math.min(iconWidth, iconHeight);
                }
                case MAC_OS -> {
                    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                    yield gd.getDefaultConfiguration().getBounds().width > 2560 ? 128 : 64;
                }
                case LINUX -> 48;
                case OTHER -> 32;
            };
        } catch (Exception e) {
            LogManager.getLogger(OS.class).warn("Failed to get icon size", e);
            return 32;
        }
    }
}
