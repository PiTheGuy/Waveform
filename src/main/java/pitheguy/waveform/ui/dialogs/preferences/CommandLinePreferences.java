package pitheguy.waveform.ui.dialogs.preferences;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.config.NotificationState;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.main.WaveColor;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.util.*;

public record CommandLinePreferences(Optional<Color> foregroundColor, Optional<Color> backgroundColor,
                                     Optional<Color> playedColor, Optional<Boolean> dynamicIcon,
                                     Optional<Boolean> highContrast, Optional<NotificationState> notifications,
                                     Optional<Boolean> mono, Optional<Boolean> disableSmoothing,
                                     Optional<Boolean> forceRead) {
    public static CommandLinePreferences EMPTY = new CommandLinePreferences(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    public void apply() {
        SettingsInstance settings = Config.settings;
        foregroundColor.ifPresent(color -> settings.setValue("foregroundColor", color));
        backgroundColor.ifPresent(color -> settings.setValue("backgroundColor", color));
        playedColor.ifPresent(color -> settings.setValue("playedColor", color));
        dynamicIcon.ifPresent(d -> settings.setValue("dynamicIcon", d));
        highContrast.ifPresent(hc -> settings.setValue("highContrast", hc));
        notifications.ifPresent(n -> settings.setValue("notifications", n));
        mono.ifPresent(m -> settings.setValue("mono", m));
        disableSmoothing.ifPresent(ds -> settings.setValue("disableSmoothing", ds));
        forceRead.ifPresent(d -> settings.setValue("forceRead", d));
        Waveform.getInstance().controller.updateColors();
    }

    public static CommandLinePreferences fromCommandLine(CommandLine commandLine) throws ParseException {
        String preferencesString = commandLine.getOptionValue("preferences");
        Builder builder = builderFromString(preferencesString);
        if (commandLine.hasOption("force")) builder.forceRead = true;
        return builder.build();
    }

    @VisibleForTesting
    static CommandLinePreferences.Builder builderFromString(String preferencesString) throws ParseException {
        Builder builder = new Builder();
        if (preferencesString == null) return builder;
        String[] parts = preferencesString.split(",");
        Set<String> addedKeys = new HashSet<>();
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
        return builder;
    }

    private static Color parseColor(String value) throws ParseException {
        Color color = Util.parseColor(value);
        if (color == null)
            throw new ParseException("Invalid color: " + value + ". Must be one of: " + Util.getEnumKeys(WaveColor.class) + ", or a hex color code");
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
            throw new ParseException("Invalid notification state: " + value + ". Must be one of: " + Util.getEnumKeys(NotificationState.class));
        }
    }

    public static class Builder {
        Color foregroundColor;
        Color backgroundColor;
        Color playedColor;
        Boolean dynamicIcon;
        Boolean highContrast;
        NotificationState notifications;
        Boolean mono;
        Boolean disableSmoothing;
        Boolean forceRead;

        public CommandLinePreferences build() {
            return new CommandLinePreferences(
                    Optional.ofNullable(foregroundColor),
                    Optional.ofNullable(backgroundColor),
                    Optional.ofNullable(playedColor),
                    Optional.ofNullable(dynamicIcon),
                    Optional.ofNullable(highContrast),
                    Optional.ofNullable(notifications),
                    Optional.ofNullable(mono),
                    Optional.ofNullable(disableSmoothing),
                    Optional.ofNullable(forceRead)
            );
        }
    }
}
