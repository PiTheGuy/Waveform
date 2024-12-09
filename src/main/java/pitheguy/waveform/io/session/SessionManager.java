package pitheguy.waveform.io.session;

import pitheguy.waveform.main.Visualizer;

import java.io.IOException;
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

    public void savePreferences(SavedPreferences preferences) throws IOException {
        session = session.withPreferences(preferences);
        session.save();
    }

    public void savePreviousVisualizers(List<Visualizer> previousVisualizers) throws IOException {
        session = session.withPreviousVisualizers(previousVisualizers);
        session.save();
    }
}
