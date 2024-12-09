package pitheguy.waveform.main.validator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CommandLineValidator {
    private final List<ValidationRule> validationRules = new ArrayList<>();

    public void validate(CommandLine commandLine) throws ParseException {
        for (ValidationRule rule : validationRules) rule.validate(commandLine);
    }

    public void addRule(ValidationRule rule) {
        validationRules.add(rule);
    }

    public void addError(boolean condition, String message) {
        addRule(ValidationRule.createError(condition).message(message));
    }

    public void addError(Supplier<Boolean> condition, String message) {
        addRule(ValidationRule.createError(condition).message(message));
    }

    public void addWarning(boolean condition, String message) {
        addRule(ValidationRule.createWarning(condition).message(message));
    }

    public void addWarning(Supplier<Boolean> condition, String message) {
        addRule(ValidationRule.createWarning(condition).message(message));
    }

}
