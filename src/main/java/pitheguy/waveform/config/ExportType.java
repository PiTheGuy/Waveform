package pitheguy.waveform.config;

public enum ExportType {
    IMAGE(".png"),
    VIDEO(".mp4"),
    GIF(".gif"),
    AUDIO(".wav");

    private final String extension;

    ExportType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
