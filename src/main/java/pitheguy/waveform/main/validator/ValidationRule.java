package pitheguy.waveform.main.validator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ValidationRule {
    private final List<String> requiredOptions = new ArrayList<>();
    private final List<String> disallowedOptions = new ArrayList<>();
    private final Supplier<Boolean> condition;
    private final boolean isWarning;
    private String message;

    protected ValidationRule(boolean isWarning) {
        this(true, isWarning);
    }

    protected ValidationRule(boolean condition, boolean isWarning) {
        this(() -> condition, isWarning);
    }

    protected ValidationRule(Supplier<Boolean> condition, boolean isWarning) {
        this.condition = condition;
        this.isWarning = isWarning;
    }

    public static ValidationRule createError() {
        return new ValidationRule(false);
    }

    public static ValidationRule createError(boolean condition) {
        return new ValidationRule(condition, false);
    }

    public static ValidationRule createError(Supplier<Boolean> condition) {
        return new ValidationRule(condition, false);
    }

    public static ValidationRule createWarning() {
        return new ValidationRule(true);
    }

    public static ValidationRule createWarning(boolean condition) {
        return new ValidationRule(condition, true);
    }

    public static ValidationRule createWarning(Supplier<Boolean> condition) {
        return new ValidationRule(condition, true);
    }

    public static ValidationRule mutuallyExclusive(String... options) {
        return new MutuallyExclusiveValidationRule(options);
    }

    /**
     * Tests this validation rule, and returns {@code true} if the validation fails.
     * @param commandLine The {@code CommandLine} object created from the user's command line arguments.
     * @return {@code true} if the validation fails, {@code false} otherwise.
     */
    public boolean test(CommandLine commandLine) {
        boolean result = condition.get();
        result &= requiredOptions.stream().allMatch(commandLine::hasOption);
        result &= disallowedOptions.stream().noneMatch(commandLine::hasOption);
        if (message == null) throw new IllegalStateException("No message specified");
        return result;
    }

    public void validate(CommandLine commandLine) throws ParseException {
        if (!test(commandLine)) return;
        if (isWarning) System.out.println("Warning: " + message);
        else throw new ParseException(message);
    }

    public ValidationRule requires(String option) {
        requiredOptions.add(option);
        return this;
    }

    public ValidationRule disallows(String option) {
        disallowedOptions.add(option);
        return this;
    }

    public ValidationRule message(String message) {
        this.message = message;
        return this;
    }
}
