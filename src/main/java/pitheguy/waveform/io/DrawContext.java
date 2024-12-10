package pitheguy.waveform.io;

public enum DrawContext {
    REALTIME,
    EXPORT_FRAME,
    EXPORT_FULL;

    public boolean isExport() {
        return this == EXPORT_FRAME || this == EXPORT_FULL;
    }
}
