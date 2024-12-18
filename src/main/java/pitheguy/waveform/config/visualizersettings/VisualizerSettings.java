package pitheguy.waveform.config.visualizersettings;

import com.google.gson.JsonObject;
import pitheguy.waveform.main.Visualizer;

import java.util.EnumMap;

public class VisualizerSettings {
    public static final EnumMap<Visualizer, VisualizerSettingsInstance> SETTINGS = new EnumMap<>(Visualizer.class);
    private static final EnumMap<Visualizer, JsonObject> SERIALIZED_SETTINGS = new EnumMap<>(Visualizer.class);

    public static VisualizerSettingsInstance getSettings(Visualizer visualizer) {
        return SETTINGS.computeIfAbsent(visualizer, VisualizerSettings::loadSettings);
    }

    private static VisualizerSettingsInstance loadSettings(Visualizer visualizer) {
        VisualizerSettingsInstance visualizerSettings = visualizer.getSettings();
        if (SERIALIZED_SETTINGS.containsKey(visualizer)) visualizerSettings.setFromJson(SERIALIZED_SETTINGS.get(visualizer));
        return visualizerSettings;
    }

    public static JsonObject saveToJson() {
        JsonObject json = new JsonObject();
        SETTINGS.forEach((visualizer, settings) -> {
            if (!settings.hasSettings()) return;
            json.add(visualizer.getKey(), settings.toJson());
        });
        SERIALIZED_SETTINGS.forEach((visualizer, settings) -> {
            if (SETTINGS.containsKey(visualizer)) return;
            json.add(visualizer.getKey(), settings);
        });
        return json;
    }

    public static void loadFromJson(JsonObject json) {
        SERIALIZED_SETTINGS.clear();
        for (String key : json.keySet()) {
            Visualizer visualizer = Visualizer.fromKey(key);
            if (visualizer == null) continue;
            JsonObject settings = json.get(key).getAsJsonObject();
            SERIALIZED_SETTINGS.put(visualizer, settings);
        }
    }
}
