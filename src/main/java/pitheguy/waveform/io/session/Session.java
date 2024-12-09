package pitheguy.waveform.io.session;

import com.google.gson.*;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.main.Visualizer;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.ui.dialogs.preferences.visualizersettings.VisualizerSettings;
import pitheguy.waveform.util.OS;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public record Session(SavedPreferences savedPreferences, List<Visualizer> previousVisualizers, List<String> suppressedWarnings) {
    public static final Session EMPTY = new Session(SavedPreferences.EMPTY, List.of(), List.of());

    void save(File file) throws IOException {
        if (!file.exists()) {
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            Files.createFile(file.toPath());
        }
        JsonObject json = new JsonObject();
        SavedPreferences saved = savedPreferences.mergeWith(load().savedPreferences);
        json.add("preferences", saved.toJson());
        JsonArray visualizers = new JsonArray();
        previousVisualizers.stream().map(Enum::name).forEach(visualizers::add);
        json.add("previousVisualizers", visualizers);
        json.add("visualizerSettings", VisualizerSettings.saveToJson());
        JsonArray suppressedWarnings = new JsonArray();
        this.suppressedWarnings.forEach(suppressedWarnings::add);
        json.add("suppressedWarnings", suppressedWarnings);
        Files.write(file.toPath(), json.toString().getBytes());
    }

    public void save() throws IOException {
        save(OS.getSessionFile());
    }

    static Session load(File file) {
        if (!file.exists()) return EMPTY;
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            SavedPreferences savedPreferences = SavedPreferences.fromJson(json.getAsJsonObject("preferences"));
            List<Visualizer> previousVisualizers = new ArrayList<>();
            List<String> suppressedWarnings = new ArrayList<>();
            if (json.has("previousVisualizers"))
                json.getAsJsonArray("previousVisualizers").forEach(visualizer -> previousVisualizers.add(Visualizer.valueOf(visualizer.getAsString())));
            if (json.has("suppressedWarnings"))
                json.getAsJsonArray("suppressedWarnings").forEach(warning -> suppressedWarnings.add(warning.getAsString()));
            if (json.has("visualizerSettings")) VisualizerSettings.loadFromJson(json.getAsJsonObject("visualizerSettings"));
            return new Session(savedPreferences, previousVisualizers, suppressedWarnings);
        } catch (Exception e) {
            e.printStackTrace();
            return EMPTY;
        }
    }

    public static Session load() {
        return load(OS.getSessionFile());
    }

    public void apply(boolean restoreVisualizer) {
        if (!Config.disablePreferences) savedPreferences.apply();
        Waveform.getInstance().menuBar.applyPreviousVisualizers(previousVisualizers);
        if (!previousVisualizers.isEmpty() && restoreVisualizer) Waveform.getInstance().switchVisualizer(previousVisualizers.getLast());
        new SessionManager(this);
    }

    public Session withPreferences(SavedPreferences savedPreferences) {
        return new Session(savedPreferences, previousVisualizers, suppressedWarnings);
    }

    public Session withPreviousVisualizers(List<Visualizer> previousVisualizers) {
        return new Session(savedPreferences, previousVisualizers, suppressedWarnings);
    }

    public Session withSuppressedWarnings(List<String> suppressedWarnings) {
        return new Session(savedPreferences, previousVisualizers, suppressedWarnings);
    }
}
