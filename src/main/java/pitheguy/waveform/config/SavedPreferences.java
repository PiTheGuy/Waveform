package pitheguy.waveform.config;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pitheguy.waveform.config.visualizersettings.SettingType;
import pitheguy.waveform.config.visualizersettings.SettingsInstance;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.util.*;
import java.util.function.Function;

public class SavedPreferences {

    private final SettingsInstance settings;
    private final Set<String> containedSettings;

    public SavedPreferences(SettingsInstance settings, Set<String> containedSettings) {
        this.settings = settings;
        this.containedSettings = containedSettings;
    }

    public static final SavedPreferences EMPTY = SavedPreferences.create(new HashMap<>());

    public static SavedPreferences create(Map<String, Object> map) {
        SettingsInstance settings = createDefaultSettings();
        settings.setFromMap(map);
        return new SavedPreferences(settings, map.keySet());
    }

    public static SettingsInstance createDefaultSettings() {
        return new SettingsInstance.Builder()
                .addSetting("backgroundColor", SettingType.COLOR, Color.BLACK)
                .addSetting("foregroundColor", SettingType.COLOR, Color.WHITE)
                .addSetting("playedColor", SettingType.COLOR, Color.RED)
                .addSetting("dynamicIcon", SettingType.BOOLEAN, true)
                .addSetting("highContrast", SettingType.BOOLEAN, false)
                .addSetting("pauseOnExport", SettingType.BOOLEAN, true)
                .addSetting("notifications", SettingType.forEnum(NotificationState.class), NotificationState.WHEN_MINIMIZED)
                .addSetting("mono", SettingType.BOOLEAN, false)
                .addSetting("disableSmoothing", SettingType.BOOLEAN, false)
                .addSetting("showInSystemTray", SettingType.BOOLEAN, true)
                .addSetting("forceRead", SettingType.BOOLEAN, false)
                .build();
    }

    public JsonObject toJson() {
        return settings.toJson();
    }

    @VisibleForTesting
    public static SavedPreferences fromJson(JsonObject json) {
        SettingsInstance settings = createDefaultSettings();
        Set<String> containedSettings = new HashSet<>(json.keySet());
        containedSettings.removeIf(s -> !settings.hasSetting(s));
        settings.setFromJson(json);
        return new SavedPreferences(settings, containedSettings);
    }

    public void apply() {
        Config.setSettings(settings);
        Waveform.getInstance().updateColors();
    }

    public <T> T getSetting(String key, Class<T> clazz) {
        return settings.getValue(key, clazz);
    }

    public boolean containsSetting(String key) {
        return containedSettings.contains(key);
    }

    public SavedPreferences mergeWith(SavedPreferences old) {
        Set<String> keys = createDefaultSettings().getSettings().keySet();
        Map<String, Object> map = new HashMap<>();
        for (String key : keys) doMerge(map, key, old);
        return SavedPreferences.create(map);
    }

    private void doMerge(Map<String, Object> map, String key, SavedPreferences old) {
        if (containedSettings.contains(key)) map.put(key, settings.getSettings().get(key).getValue());
        else if (old.containedSettings.contains(key)) map.put(key, old.settings.getSettings().get(key).getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SavedPreferences other)) return false;
        if (!other.containedSettings.equals(containedSettings)) return false;
        for (String key : containedSettings)
            if (!other.settings.getSettings().get(key).getValue().equals(settings.getSettings().get(key).getValue()))
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = containedSettings.hashCode();
        for (String key : containedSettings) {
            Object value = settings.getSettings().get(key).getValue();
            result = 31 * result + (value == null ? 0 : value.hashCode());
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (String key : containedSettings) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            Object value = settings.getSettings().get(key).getValue();
            sb.append(key).append("=").append(value);
        }
        sb.append("}");
        return sb.toString();
    }
}
