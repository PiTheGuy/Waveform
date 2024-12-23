package pitheguy.waveform.config;

import pitheguy.waveform.ui.Waveform;

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

    public boolean shouldNotify() {
        return switch(this) {
            case ALWAYS -> true;
            case WHEN_MINIMIZED -> Waveform.getInstance().isMinimized();
            case NEVER -> false;
        };
    }
}
