package pitheguy.waveform.ui.dialogs.preferences;

import org.apache.commons.cli.ParseException;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.config.NotificationState;
import pitheguy.waveform.main.WaveColor;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.util.*;

public record CommandLinePreferences(Optional<Color> foregroundColor, Optional<Color> backgroundColor,
                                     Optional<Color> playedColor, Optional<Boolean> dynamicIcon,
                                     Optional<Boolean> highContrast, Optional<NotificationState> notifications,
                                     Optional<Boolean> mono, Optional<Boolean> disableSmoothing) {
    public static CommandLinePreferences EMPTY = new CommandLinePreferences(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    public void apply() {
        foregroundColor.ifPresent(color -> Config.foregroundColor = color);
        backgroundColor.ifPresent(color -> Config.backgroundColor = color);
        playedColor.ifPresent(color -> Config.playedColor = color);
        dynamicIcon.ifPresent(dynamicIcon -> Config.dynamicIcon = dynamicIcon);
        highContrast.ifPresent(highContrast -> Config.highContrast = highContrast);
        mono.ifPresent(mono -> Config.mono = mono);
        disableSmoothing.ifPresent(disableSmoothing -> Config.disableSmoothing = disableSmoothing);
        Waveform.getInstance().updateColors();
    }

    public static CommandLinePreferences fromString(String string) throws ParseException {
        if (string == null) return EMPTY;
        String[] parts = string.split(",");
        Set<String> addedKeys = new HashSet<>();
        Builder builder = new Builder();
        for (String part : parts) {
            String[] keyValue = part.split("=");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            if (addedKeys.contains(key)) throw new ParseException("Duplicate key in preferences");
            addedKeys.add(key);
            switch (key) {
                case "foregroundColor" -> builder.foregroundColor = parseColor(value);
                case "backgroundColor" -> builder.backgroundColor = parseColor(value);
                case "playedColor" -> builder.playedColor = parseColor(value);
                case "dynamicIcon" -> builder.dynamicIcon = parseBoolean(value);
                case "highContrast" -> builder.highContrast = parseBoolean(value);
                case "notifications" -> builder.notifications = parseNotifications(value);
                case "mono" -> builder.mono = parseBoolean(value);
                case "disableSmoothing" -> builder.disableSmoothing = parseBoolean(value);
                default -> throw new ParseException("Unknown key: " + key);
            }
        }
        return builder.build();
    }

    private static Color parseColor(String value) throws ParseException {
        Color color = Util.parseColor(value);
        if (color == null) throw new ParseException("Invalid color: " + value + ". Must be one of: " + WaveColor.getAvailableColors() + ", or a hex color code");
        return color;
    }

    private static boolean parseBoolean(String value) throws ParseException {
        return switch (value) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new ParseException("Invalid value: " + value + ". Must be true or false");
        };
    }

    private static NotificationState parseNotifications(String value) throws ParseException {
        try {
            return NotificationState.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid notification state: " + value + ". Must be one of: " + NotificationState.getKeys());
        }
    }

    private static class Builder {
        Color foregroundColor;
        Color backgroundColor;
        Color playedColor;
        Boolean dynamicIcon;
        Boolean highContrast;
        NotificationState notifications;
        Boolean mono;
        Boolean disableSmoothing;

        public CommandLinePreferences build() {
            return new CommandLinePreferences(
                    Optional.ofNullable(foregroundColor),
                    Optional.ofNullable(backgroundColor),
                    Optional.ofNullable(playedColor),
                    Optional.ofNullable(dynamicIcon),
                    Optional.ofNullable(highContrast),
                    Optional.ofNullable(notifications),
                    Optional.ofNullable(mono),
                    Optional.ofNullable(disableSmoothing)
            );
        }
    }
}
