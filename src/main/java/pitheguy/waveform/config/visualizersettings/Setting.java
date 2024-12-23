package pitheguy.waveform.config.visualizersettings;

import com.google.gson.JsonElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Setting<T> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String name;
    private final SettingType<T> type;
    private T value;
    private final T defaultValue;

    public Setting(String name, SettingType<T> type, T value, T defaultValue) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.defaultValue = defaultValue;
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

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setValue(T value) {
        if (value == null || !type.isValid(value)) {
            LOGGER.warn("Attempted to set invalid value for setting: {}", name);
            value = defaultValue;
        }
        this.value = value;
    }

    public JsonElement serialize() {
        return type.serialize(value);
    }

    public Object deserialize(JsonElement jsonElement) {
        return type.deserialize(jsonElement);
    }
}
