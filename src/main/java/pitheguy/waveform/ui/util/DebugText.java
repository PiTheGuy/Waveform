package pitheguy.waveform.ui.util;

import java.util.ArrayList;
import java.util.List;

public class DebugText {
    private final List<Entry> entries = new ArrayList<>();

    public DebugText add(String name, String value) {
        entries.add(new Entry(name, value));
        return this;
    }

    public DebugText add(String name, double value) {
        return add(name, "" + value);
    }

    public DebugText add(String name, int value) {
        return add(name, "" + value);
    }

    public String getText() {
        return String.join("\n", entries.stream().map(Entry::toString).toList());
    }

    public record Entry(String name, String value) {
        public String toString() {
            return name + ": " + value;
        }
    }
}
