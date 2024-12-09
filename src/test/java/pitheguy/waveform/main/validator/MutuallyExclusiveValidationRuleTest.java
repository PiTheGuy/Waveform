package pitheguy.waveform.main.validator;

import org.apache.commons.cli.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MutuallyExclusiveValidationRuleTest {

    private Options options;

    @BeforeEach
    public void setup() {
        // Setup the command-line options for the tests
        options = new Options();
        options.addOption("a", false, "Option A");
        options.addOption("b", false, "Option B");
        options.addOption("c", false, "Option C");
    }

    private CommandLine parseCommandLine(String... args) throws Exception {
        return new DefaultParser().parse(options, args);
    }

    @Test
    public void testSingleOptionProvided() throws Exception {
        CommandLine cmd = parseCommandLine("-a");
        MutuallyExclusiveValidationRule rule = new MutuallyExclusiveValidationRule("a", "b", "c");
        boolean result = rule.test(cmd);
        assertFalse(result, "Expected validation to pass when only one option is provided.");
    }

    @Test
    public void testNoOptionsProvided() throws Exception {
        CommandLine cmd = parseCommandLine();
        MutuallyExclusiveValidationRule rule = new MutuallyExclusiveValidationRule("a", "b", "c");
        boolean result = rule.test(cmd);
        assertFalse(result, "Expected validation to pass when no options are provided.");
    }

    @Test
    public void testMultipleOptionsProvided() throws Exception {
        CommandLine cmd = parseCommandLine("-a", "-b");

        MutuallyExclusiveValidationRule rule = new MutuallyExclusiveValidationRule("a", "b", "c");
        boolean result = rule.test(cmd);

        // Validation fails, so test() should return true
        assertTrue(result, "Expected validation to fail when multiple options are provided.");
    }

    @Test
    public void testAllOptionsProvided() throws Exception {
        CommandLine cmd = parseCommandLine("-a", "-b", "-c");

        MutuallyExclusiveValidationRule rule = new MutuallyExclusiveValidationRule("a", "b", "c");
        boolean result = rule.test(cmd);

        // Validation fails, so test() should return true
        assertTrue(result, "Expected validation to fail when all options are provided.");
    }
}