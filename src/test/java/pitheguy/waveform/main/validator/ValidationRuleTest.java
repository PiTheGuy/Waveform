package pitheguy.waveform.main.validator;

import org.apache.commons.cli.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationRuleTest {
    @Test
    void testNoMessageFails() throws Exception {
        CommandLine commandLine = new DefaultParser().parse(new Options(), new String[]{});
        ValidationRule rule = ValidationRule.createWarning();
        assertThrows(IllegalStateException.class, () -> rule.validate(commandLine));
    }

    @Test
    void testRequiredOptionsAllPresent() throws Exception {
        // Create options with "a" and "b" provided.
        Options options = new Options();
        options.addOption("a", false, "Option A");
        options.addOption("b", false, "Option B");
        CommandLine commandLine = new DefaultParser().parse(options, new String[]{"-a", "-b"});

        // Define a rule that requires both "a" and "b".
        ValidationRule rule = ValidationRule.createError(() -> true)
                .requires("a")
                .requires("b")
                .message("Can't have both A and B");

        // Since both "a" and "b" are present, an error should be thrown.
        assertThrows(ParseException.class, () -> rule.validate(commandLine));
    }

    @Test
    void testMissingRequiredOption() throws Exception {
        // Create options with only "a" provided (missing "b").
        Options options = new Options();
        options.addOption("a", false, "Option A");
        CommandLine commandLine = new DefaultParser().parse(options, new String[]{"-a"});

        // Define a rule that requires both "a" and "b".
        ValidationRule rule = ValidationRule.createError(() -> true)
                .requires("a")
                .requires("b")
                .message("Can have both A and B");

        // Since "b" is missing, the rule passes silently.
        rule.validate(commandLine); // Should pass silently.
    }

    @Test
    void testDisallowedOptionPresent() throws Exception {
        // Create options with "b" provided (which is disallowed).
        Options options = new Options();
        options.addOption("b", false, "Option B");
        CommandLine commandLine = new DefaultParser().parse(options, new String[]{"-b"});

        // Define a rule that disallows "b".
        ValidationRule rule = ValidationRule.createError(() -> true)
                .disallows("b")
                .message("Must have option B");

        // Since "b" is present, an error should be thrown.
        rule.validate(commandLine);
    }

    @Test
    void testDisallowedOptionAbsent() throws Exception {
        // Create options with only "a" provided (no disallowed options).
        Options options = new Options();
        options.addOption("a", false, "Option A");
        CommandLine commandLine = new DefaultParser().parse(options, new String[]{"-a"});

        // Define a rule that disallows "b".
        ValidationRule rule = ValidationRule.createError(() -> true)
                .disallows("b")
                .message("Must have option B");

        // Since "b" is absent, the rule passes silently.
        assertThrows(ParseException.class, () -> rule.validate(commandLine)); // Should pass silently.
    }
}