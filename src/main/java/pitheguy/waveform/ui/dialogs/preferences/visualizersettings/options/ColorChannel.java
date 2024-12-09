package pitheguy.waveform.ui.dialogs.preferences.visualizersettings.options;

public enum ColorChannel {
    RED("Red", 16),
    GREEN("Green", 8),
    BLUE("Blue", 0);

    private final String name;
    private final int shift;

    ColorChannel(String name, int shift) {
        this.name = name;
        this.shift = shift;
    }

    @Override
    public String toString() {
        return name;
    }

    public int shift(double value) {
        return (int) (value * 255 + 0.5) << shift;
    }

    public int getShift() {
        return shift;
    }
}
