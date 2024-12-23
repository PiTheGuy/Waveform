package pitheguy.waveform.main;

import java.awt.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public enum WaveColor {
    BLACK(Color.BLACK),
    WHITE(Color.WHITE),
    RED(Color.RED),
    ORANGE(Color.ORANGE),
    YELLOW(Color.YELLOW),
    GREEN(Color.GREEN),
    BLUE(Color.BLUE),
    MAGENTA(Color.MAGENTA),
    CYAN(Color.CYAN),
    PINK(Color.PINK),
    LIGHT_GRAY(Color.LIGHT_GRAY),
    GRAY(Color.GRAY),
    DARK_GRAY(Color.DARK_GRAY);

    private final Color color;

    WaveColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public static WaveColor fromName(String name) {
        try {
            return WaveColor.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Optional<WaveColor> fromColor(Color color) {
        return Arrays.stream(WaveColor.values()).filter(waveColor -> waveColor.getColor().equals(color)).findFirst();
    }

    public String getHumanName() {
        return Arrays.stream(toString().split("_")).map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase() + " ").collect(Collectors.joining()).trim();
    }

    public static WaveColor fromHumanName(String name) {
        return fromName(name.replaceAll(" ", "_"));
    }
}
