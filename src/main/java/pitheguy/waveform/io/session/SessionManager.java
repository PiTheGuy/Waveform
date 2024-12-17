package pitheguy.waveform.io.session;

import pitheguy.waveform.main.Visualizer;
import pitheguy.waveform.util.Util;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static SessionManager instance;
    private Session session;

    public SessionManager(Session session) {
        this.session = session;
        instance = this;
    }

    public static SessionManager getInstance() {
        return instance;
    }

    public void savePreferences(SavedPreferences preferences) {
        session = session.withPreferences(preferences);
        Util.showErrorOnException(session::save, "Failed to save preferences");
    }

    public void savePreviousVisualizers(List<Visualizer> previousVisualizers) {
        session = session.withPreviousVisualizers(previousVisualizers);
        Util.showErrorOnException(session::save, "Failed to save previous visualizers");
    }

    public void suppressWarning(String key) {
        List<String> suppressedWarnings = new ArrayList<>(session.suppressedWarnings());
        suppressedWarnings.add(key);
        session = session.withSuppressedWarnings(suppressedWarnings);
        Util.showErrorOnException(session::save, "Failed to suppress warning");
    }

    public boolean isWarningSuppressed(String key) {
        return session.suppressedWarnings().contains(key);
    }
}
