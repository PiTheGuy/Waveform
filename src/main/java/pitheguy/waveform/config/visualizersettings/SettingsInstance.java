package pitheguy.waveform.config.visualizersettings;

import com.google.gson.*;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsInstance {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<String, Setting<?>> settings;

    private SettingsInstance(Map<String, Setting<?>> settings) {
        this.settings = settings;
    }

    public Map<String, Setting<?>> getSettings() {
        return settings;
    }

    public boolean hasSettings() {
        return !settings.isEmpty();
    }

    public boolean hasSetting(String key) {
        return settings.containsKey(key);
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
            Object value = setting.deserialize(json.get(key));
            Setting<Object> castSetting = (Setting<Object>) setting;
            castSetting.setValue(value);
        }
    }

    public void setFromMap(Map<String, Object> map) {
        map.forEach((key, value) -> {
            if (!settings.containsKey(key)) {
                LOGGER.warn("Attempted to set unknown setting: {}", key);
                return;
            }
            Setting<?> setting = settings.get(key);
            setSettingValue(key, value, setting);
        });
    }

    public <T> T getValue(String key, Class<T> clazz) {
        Setting<?> setting = settings.get(key);
        if (setting == null) throw new IllegalArgumentException("No setting found for key: " + key);
        if (!clazz.isAssignableFrom(setting.getType().getClazz()))
            throw new ClassCastException("Setting type mismatch for key: " + key);
        return clazz.cast(setting.getValue());
    }

    public void setValue(String key, Object value) {
        Setting<?> setting = settings.get(key);
        if (setting == null) throw new IllegalArgumentException("No setting found for key: " + key);
        setSettingValue(key, value, setting);
    }

    private void setSettingValue(String key, Object value, Setting<?> setting) {
        Class<?> settingClass = setting.getType().getClazz();
        if (settingClass.isInstance(value)) {
            Setting<Object> castSetting = (Setting<Object>) setting;
            castSetting.setValue(value);
        } else {
            LOGGER.warn("Attempted to set setting {} to incorrect type (expected: {}, found: {})", key, settingClass.getName(), value.getClass().getName());
            System.out.println(value);
        }
    }

    public static class Builder {
        private final Map<String, Setting<?>> settings = new LinkedHashMap<>();

        public <T> Builder addSetting(String key, SettingType<T> type, T defaultValue) {
            String name;
            if (key.contains("_"))
                name = Character.toUpperCase(key.charAt(0)) + key.substring(1).replace("_", " ").toLowerCase();
            else {
                name = key.replaceAll("([a-z])([A-Z])", "$1 $2");
                name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }
            return addSetting(key, name, type, defaultValue);
        }

        public <T> Builder addSetting(String key, String name, SettingType<T> type, T defaultValue) {
            settings.put(key, new Setting<>(name, type, defaultValue, defaultValue));
            return this;
        }

        public SettingsInstance build() {
            return new SettingsInstance(settings);
        }
    }

}
