package pitheguy.waveform.util;

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
}
