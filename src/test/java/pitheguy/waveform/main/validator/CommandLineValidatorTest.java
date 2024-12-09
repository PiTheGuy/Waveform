package pitheguy.waveform.main.validator;

import org.apache.commons.cli.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineValidatorTest {
    @Test
    void testAddErrorConditionFails() throws Exception {
        // Create a command line without any options provided.
        Options options = new Options();
        CommandLine commandLine = new DefaultParser().parse(options, new String[0]);

        // Create a CommandLineValidator and add an error rule that expects "a".
        CommandLineValidator validator = new CommandLineValidator();
        validator.addError(false, "This error should not trigger");

        // Since the condition is false, no error should be thrown.
        assertDoesNotThrow(() -> validator.validate(commandLine));
    }

    @Test
    void testAddErrorConditionPasses() throws ParseException {
        // Create a command line without any options provided.
        Options options = new Options();
        CommandLine commandLine = new DefaultParser().parse(options, new String[0]);

        // Create a CommandLineValidator and add an error rule with a passing condition.
        CommandLineValidator validator = new CommandLineValidator();
        validator.addError(true, "This error should trigger");

        // Since the condition is true, a ParseException should be thrown.
        assertThrows(ParseException.class, () -> validator.validate(commandLine));
    }

    @Test
    void testAddWarningConditionFails() throws Exception {
        // Create a command line without any options provided.
        Options options = new Options();
        CommandLine commandLine = new DefaultParser().parse(options, new String[0]);

        // Create a CommandLineValidator and add a warning rule that expects "a".
        CommandLineValidator validator = new CommandLineValidator();
        validator.addWarning(false, "This warning should not trigger");

        // Since the condition is false, no warning should be logged, and no exceptions thrown.
        assertDoesNotThrow(() -> validator.validate(commandLine));
    }

    @Test
    void testAddWarningConditionPasses() throws Exception {
        // Create a command line without any options provided.
        Options options = new Options();
        CommandLine commandLine = new DefaultParser().parse(options, new String[0]);

        // Create a CommandLineValidator and add a warning rule with a passing condition.
        CommandLineValidator validator = new CommandLineValidator();
        validator.addWarning(true, "This warning should trigger");

        // Since the condition is true, a warning message will be printed, but no exception should be thrown.
        assertDoesNotThrow(() -> validator.validate(commandLine));
    }

    @Test
    void testAddWarningWithSupplierConditionFails() throws Exception {
        // Create a command line without any options provided.
        Options options = new Options();
        CommandLine commandLine = new DefaultParser().parse(options, new String[0]);

        // Create a CommandLineValidator and add a warning rule with a failing supplier condition.
        CommandLineValidator validator = new CommandLineValidator();
        validator.addWarning(() -> false, "This supplier warning should not trigger");

        // Since the condition is false, no warning should be logged, and no exceptions thrown.
        assertDoesNotThrow(() -> validator.validate(commandLine));
    }

    @Test
    void testAddWarningWithSupplierConditionPasses() throws Exception {
        // Create a command line without any options provided.
        Options options = new Options();
        CommandLine commandLine = new DefaultParser().parse(options, new String[0]);

        // Create a CommandLineValidator and add a warning rule with a passing supplier condition.
        CommandLineValidator validator = new CommandLineValidator();
        validator.addWarning(() -> true, "This supplier warning should trigger");

        // Since the condition is true, a warning message will be printed, but no exception should be thrown.
        assertDoesNotThrow(() -> validator.validate(commandLine));
    }

    @Test
    void testMultipleValidationRules() throws Exception {
        // Create a command line with the "a" option provided.
        Options options = new Options();
        options.addOption("a", false, "Option A");
        CommandLine commandLine = new DefaultParser().parse(options, new String[]{"-a"});

        // Create a CommandLineValidator and add multiple rules.
        CommandLineValidator validator = new CommandLineValidator();
        validator.addWarning(() -> true, "This warning should trigger");
        validator.addError(() -> commandLine.hasOption("a"), "Option A should trigger an error");

        // The error rule will trigger due to the presence of option "a".
        assertThrows(ParseException.class, () -> validator.validate(commandLine));
    }



}