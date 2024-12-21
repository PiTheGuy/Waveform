package pitheguy.waveform.config;

import pitheguy.waveform.ui.Waveform;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum NotificationState {
    ALWAYS("Always"),
    WHEN_MINIMIZED("When Minimized"),
    NEVER("Never");

    private final String name;

    NotificationState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static String getKeys() {
        return Arrays.stream(NotificationState.values()).map(v -> v.name().toLowerCase()).collect(Collectors.joining(", "));
    }

    public boolean shouldNotify() {
        return switch(this) {
            case ALWAYS -> true;
            case WHEN_MINIMIZED -> Waveform.getInstance().isMinimized();
            case NEVER -> false;
        };
    }
}
