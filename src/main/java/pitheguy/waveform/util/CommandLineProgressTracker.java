package pitheguy.waveform.util;

public class CommandLineProgressTracker implements ProgressTracker {

    private final String message;
    private final int maxValue;
    private int progress = 0;

    public CommandLineProgressTracker(String message, int maxValue) {
        this.message = message;
        this.maxValue = maxValue;
    }

    @Override
    public void step() {
        progress++;
        System.out.print("\r" + getMessage());
    }

    private String getMessage() {
        double percent = progress * 100.0 / maxValue;
        return "%s (%.2f%%)".formatted(message, percent);
    }

    @Override
    public void setText(String text) {
        System.out.println(text);
    }
}
