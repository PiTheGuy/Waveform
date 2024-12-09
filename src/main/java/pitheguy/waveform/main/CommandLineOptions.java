package pitheguy.waveform.main;

import org.apache.commons.cli.*;

public class CommandLineOptions {
    private final Options allOptions = new Options();
    private final Options visibleOptions = new Options();

    public void addOption(String option, String description) {
        addOption(option, false, description);
    }

    public void addOption(String option, boolean hasArg, String description) {
        allOptions.addOption(option, hasArg, description);
        visibleOptions.addOption(option, hasArg, description);
    }

    public void addOption(Option option) {
        allOptions.addOption(option);
        visibleOptions.addOption(option);
    }

    public void addHiddenOption(String option, String description) {
        addHiddenOption(option, false, description);
    }

    public void addHiddenOption(String option, boolean hasArg, String description) {
        allOptions.addOption(option, hasArg, description);
    }

    public Options getAllOptions() {
        return allOptions;
    }

    public Options getVisibleOptions() {
        return visibleOptions;
    }
}
