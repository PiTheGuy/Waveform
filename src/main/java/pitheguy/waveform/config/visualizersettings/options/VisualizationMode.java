package pitheguy.waveform.config.visualizersettings.options;

public enum VisualizationMode {
    INSTANTANEOUS("Instantaneous"),
    GRAPH("Graph");

    private final String name;

    VisualizationMode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}