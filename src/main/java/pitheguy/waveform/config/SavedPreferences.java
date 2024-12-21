package pitheguy.waveform.config;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.util.Optional;
import java.util.function.Function;

public record SavedPreferences(Optional<Color> backgroundColor, Optional<Color> foregroundColor,
                               Optional<Color> playedColor, Optional<Boolean> dynamicIcon,
                               Optional<Boolean> highContrast, Optional<Boolean> pauseOnExport,
                               Optional<NotificationState> notifications, Optional<Boolean> mono,
                               Optional<Boolean> disableSmoothing, Optional<Boolean> showInSystemTray) {

    public static final SavedPreferences EMPTY = new SavedPreferences(
            java.util.Optional.empty(),
            java.util.Optional.empty(),
            java.util.Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    public static SavedPreferences create(Color backgroundColor, Color foregroundColor, Color playedColor,
                                          boolean dynamicIcon, boolean highContrast, boolean pauseOnExport,
                                          NotificationState notifications, boolean mono, boolean disableSmoothing,
                                          boolean showInSystemTray) {
        return new SavedPreferences(
                Optional.of(backgroundColor),
                Optional.of(foregroundColor),
                Optional.of(playedColor),
                Optional.of(dynamicIcon),
                Optional.of(highContrast),
                Optional.of(pauseOnExport),
                Optional.of(notifications),
                Optional.of(mono),
                Optional.of(disableSmoothing),
                Optional.of(showInSystemTray));
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        backgroundColor.ifPresent(color -> json.addProperty("backgroundColor", Util.writeColor(color)));
        foregroundColor.ifPresent(color -> json.addProperty("foregroundColor", Util.writeColor(color)));
        playedColor.ifPresent(color -> json.addProperty("playedColor", Util.writeColor(color)));
        dynamicIcon.ifPresent(d -> json.addProperty("dynamicIcon", d));
        highContrast.ifPresent(h -> json.addProperty("highContrast", h));
        pauseOnExport.ifPresent(pa -> json.addProperty("pauseOnExport", pa));
        notifications.ifPresent(n -> json.addProperty("notifications", n.name()));
        mono.ifPresent(m -> json.addProperty("mono", m));
        disableSmoothing.ifPresent(d -> json.addProperty("disableSmoothing", d));
        showInSystemTray.ifPresent(s -> json.addProperty("showInSystemTray", s));
        return json;
    }

    @VisibleForTesting
    public static SavedPreferences fromJson(JsonObject json) {
        Optional<Color> backgroundColor = parseOption(json, "backgroundColor", SavedPreferences::parseColor);
        Optional<Color> foregroundColor = parseOption(json, "foregroundColor", SavedPreferences::parseColor);
        Optional<Color> playedColor = parseOption(json, "playedColor", SavedPreferences::parseColor);
        Optional<Boolean> dynamicIcon = parseOption(json, "dynamicIcon", JsonElement::getAsBoolean);
        Optional<Boolean> highContrast = parseOption(json, "highContrast", JsonElement::getAsBoolean);
        Optional<Boolean> pauseOnExport = parseOption(json, "pauseOnExport", JsonElement::getAsBoolean);
        Optional<NotificationState> notifications = parseOption(json, "notifications", SavedPreferences::parseNotification);
        Optional<Boolean> mono = parseOption(json, "mono", JsonElement::getAsBoolean);
        Optional<Boolean> disableSmoothing = parseOption(json, "disableSmoothing", JsonElement::getAsBoolean);
        Optional<Boolean> showInSystemTray = parseOption(json, "showInSystemTray", JsonElement::getAsBoolean);
        return new SavedPreferences(backgroundColor, foregroundColor, playedColor, dynamicIcon, highContrast, pauseOnExport, notifications, mono, disableSmoothing, showInSystemTray);
    }

    public void apply() {
        backgroundColor.ifPresent(color -> Config.backgroundColor = color);
        foregroundColor.ifPresent(color -> Config.foregroundColor = color);
        playedColor.ifPresent(color -> Config.playedColor = color);
        dynamicIcon.ifPresent(dynamicIcon -> Config.dynamicIcon = dynamicIcon);
        highContrast.ifPresent(highContrast -> Config.highContrast = highContrast);
        pauseOnExport.ifPresent(pauseOnExport -> Config.pauseOnExport = pauseOnExport);
        notifications.ifPresent(notifications -> Config.notifications = notifications);
        mono.ifPresent(Config::setMono);
        disableSmoothing.ifPresent(disableSmoothing -> Config.disableSmoothing = disableSmoothing);
        showInSystemTray.ifPresent(Config::setShowInSystemTray);
        Waveform.getInstance().updateColors();
    }

    private static Color parseColor(JsonElement element) {
        return Util.parseColor(element.getAsString());
    }

    private static NotificationState parseNotification(JsonElement element) {
        try {
            return NotificationState.valueOf(element.getAsString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static <T> Optional<T> parseOption(JsonObject json, String option, Function<JsonElement, T> parser) {
        if (!json.has(option)) return Optional.empty();
        return Optional.ofNullable(parser.apply(json.get(option)));
    }

    public SavedPreferences mergeWith(SavedPreferences old) {
        Optional<Color> backgroundColor = backgroundColor().or(old::backgroundColor);
        Optional<Color> foregroundColor = foregroundColor().or(old::foregroundColor);
        Optional<Color> playedColor = playedColor().or(old::playedColor);
        Optional<Boolean> dynamicIcon = dynamicIcon().or(old::dynamicIcon);
        Optional<Boolean> highContrast = highContrast().or(old::highContrast);
        Optional<Boolean> pauseOnExport = pauseOnExport().or(old::pauseOnExport);
        Optional<NotificationState> notifications = notifications().or(old::notifications);
        Optional<Boolean> mono = mono().or(old::mono);
        Optional<Boolean> disableSmoothing = disableSmoothing().or(old::disableSmoothing);
        Optional<Boolean> showInSystemTray = showInSystemTray().or(old::showInSystemTray);
        return new SavedPreferences(backgroundColor, foregroundColor, playedColor, dynamicIcon, highContrast, pauseOnExport, notifications, mono, disableSmoothing, showInSystemTray);
    }
}
