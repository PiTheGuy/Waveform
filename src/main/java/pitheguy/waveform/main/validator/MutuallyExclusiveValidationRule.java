package pitheguy.waveform.main.validator;

import org.apache.commons.cli.CommandLine;

import java.util.Arrays;
import java.util.List;

public class MutuallyExclusiveValidationRule extends ValidationRule {
    private final List<String> options;

    protected MutuallyExclusiveValidationRule(String... options) {
        super(false);
        this.options = Arrays.asList(options);
    }

    @Override
    public boolean test(CommandLine commandLine) {
        return options.stream().filter(commandLine::hasOption).count() > 1;
    }
}
