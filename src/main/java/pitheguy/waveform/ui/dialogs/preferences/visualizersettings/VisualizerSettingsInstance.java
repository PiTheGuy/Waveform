package pitheguy.waveform.ui.dialogs.preferences.visualizersettings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class VisualizerSettingsInstance {
    private final Map<String, Setting<?>> settings;

    private VisualizerSettingsInstance(Map<String, Setting<?>> settings) {
        this.settings = settings;
    }

    public Map<String, Setting<?>> getSettings() {
        return settings;
    }

    public boolean hasSettings() {
        return !settings.isEmpty();
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, Setting<?>> entry : settings.entrySet()) {
            String key = entry.getKey();
            Setting<?> setting = entry.getValue();
            JsonElement element = setting.getType().serialize(setting.getValue());
            json.add(key, element);
        }
        return json;
    }

    public void setFromJson(JsonObject json) {
        for (String key : json.keySet()) {
            if (!settings.containsKey(key)) continue;
            Setting<?> setting = settings.get(key);
            Object value = setting.getType().deserialize(json.get(key));
            if (setting.getType().getClazz().isInstance(value)) {
                Setting<Object> castSetting = (Setting<Object>) setting;
                castSetting.setValue(value);
            } else {
                throw new IllegalArgumentException(
                        "Type mismatch for setting key: " + key +
                        ". Expected: " + setting.getType().getClazz().getName() +
                        ", Found: " + value.getClass().getName()
                );
            }
        }
    }

    public <T> T getValue(String key, Class<T> clazz) {
        Setting<?> setting = settings.get(key);
        if (setting == null) {
            throw new IllegalArgumentException("No setting found for key: " + key);
        }
        if (!clazz.isAssignableFrom(setting.type.getClazz())) {
            throw new ClassCastException("Setting type mismatch for key: " + key);
        }
        return clazz.cast(setting.value);
    }

    public static class Builder {
        private final Map<String, Setting<?>> settings = new LinkedHashMap<>();

        public <T> Builder addSetting(String key, SettingType<T> type, T defaultValue) {
            String name = Character.toUpperCase(key.charAt(0)) + key.substring(1).replace("_", " ");
            return addSetting(key, name, type, defaultValue);
        }

        public <T> Builder addSetting(String key, String name, SettingType<T> type, T defaultValue) {
            settings.put(key, new Setting<>(name, type, defaultValue));
            return this;
        }

        public VisualizerSettingsInstance build() {
            return new VisualizerSettingsInstance(settings);
        }
    }

    public static class Setting<T> {
        private final String name;
        private final SettingType<T> type;
        private T value;

        public Setting(String name, SettingType<T> type, T value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public SettingType<T> getType() {
            return type;
        }

        public boolean isValid(T value) {
            return type.isValid(value);
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            if (!isValid(value)) throw new IllegalArgumentException("Invalid value: " + value);
            this.value = value;
        }
    }

}
