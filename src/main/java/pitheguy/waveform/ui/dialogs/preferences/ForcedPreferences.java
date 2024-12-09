package pitheguy.waveform.ui.dialogs.preferences;

import org.apache.commons.cli.CommandLine;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.util.Optional;
import java.util.function.Function;

public record ForcedPreferences(Optional<Color> foregroundColor, Optional<Color> backgroundColor,
                                Optional<Color> playedColor, Optional<Boolean> dynamicIcon,
                                Optional<Boolean> highContrast) {
    public static ForcedPreferences EMPTY = new ForcedPreferences(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public void apply() {
        foregroundColor.ifPresent(color -> Config.foregroundColor = color);
        backgroundColor.ifPresent(color -> Config.backgroundColor = color);
        playedColor.ifPresent(color -> Config.playedColor = color);
        dynamicIcon.ifPresent(dynamicIcon -> Config.disableDynamicIcon = !dynamicIcon);
        highContrast.ifPresent(highContrast -> Config.highContrast = highContrast);
    }

    public static ForcedPreferences fromCommandLine(CommandLine commandLine) {
        Optional<Color> foregroundColor = parseOption(commandLine, "foregroundColor", Util::parseColor);
        Optional<Color> backgroundColor = parseOption(commandLine, "backgroundColor", Util::parseColor);
        Optional<Color> playedColor = parseOption(commandLine, "playedColor", Util::parseColor);
        Optional<Boolean> dynamicIcon = parseBoolean(commandLine, "disableDynamicIcon", false);
        Optional<Boolean> highContrast = parseBoolean(commandLine, "highContrast", true);
        return new ForcedPreferences(foregroundColor, backgroundColor, playedColor, dynamicIcon, highContrast);
    }

    private static <T> Optional<T> parseOption(CommandLine commandLine, String option, Function<String, T> parser) {
        if (!commandLine.hasOption(option)) return Optional.empty();
        return Optional.ofNullable(parser.apply(commandLine.getOptionValue(option)));
    }

    private static Optional<Boolean> parseBoolean(CommandLine commandLine, String option, boolean value) {
        return commandLine.hasOption(option) ? Optional.of(value) : Optional.empty();
    }
}
