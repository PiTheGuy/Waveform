package pitheguy.waveform.ui.visualizer;

import org.apache.commons.cli.*;
import pitheguy.waveform.main.Main;
import pitheguy.waveform.main.Visualizer;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.FileUtil;
import pitheguy.waveform.util.OS;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class VisualizerIconCollector {

    public static void main(String[] args) throws Exception {
        System.setProperty("PROGRAM_DATA_PATH", OS.getProgramDataPath().toString());
        Options options = createOptions();
        CommandLine commandLine = new DefaultParser().parse(options, args);
        if (commandLine.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar VisualizerIconCollector.jar", options);
            return;
        }

        Path outputPath = Path.of(commandLine.getOptionValue("output"));
        List<Visualizer> visualizers = collectVisualizers(commandLine);
        boolean overwrite = commandLine.hasOption("overwrite");
        boolean verbose = commandLine.hasOption("verbose");
        if (commandLine.hasOption("clean")) clean(outputPath);
        if (commandLine.hasOption("input")) {
            File inputFile = new File(commandLine.getOptionValue("input"));
            createIcons(inputFile, outputPath, visualizers, overwrite, verbose);
        }
        System.exit(0);
    }

    private static void clean(Path path) {
        System.out.print("WARNING: This a potentially destructive operation. Are you sure you want to do this? (y/n) ");
        Scanner scanner = new Scanner(System.in);
        if (scanner.next().equalsIgnoreCase("y")) {
            System.out.println("Cleaning started.");
            File[] files = path.toFile().listFiles(file -> file.getName().endsWith(".png"));
            if (files == null) {
                System.err.println("Error reading output directory, cleaning skipped.");
                return;
            }
            for (File file : files) {
                if (file.getName().equals("placeholder.png")) continue;
                String filename = FileUtil.stripExtension(file.getName());
                if (Visualizer.fromKey(filename) == null) {
                    if (file.delete()) System.out.println("Deleted " + file.getName());
                    else System.err.println("Error deleting " + file.getName());
                }
            }
        } else System.out.println("Cleaning skipped.");
        scanner.close();
    }

    private static void createIcons(File inputFile, Path outputPath, List<Visualizer> visualizers, boolean overwrite, boolean verbose) {
        List<Visualizer> manualVisualizers = new ArrayList<>();
        int imagesCreated = 0;
        for (Visualizer visualizer : visualizers) {
            File outputFile = outputPath.resolve(visualizer.getKey() + ".png").toFile();
            if (!overwrite && outputFile.exists()) {
                if (verbose) System.out.println("Skipped icon for " + visualizer.getName() + "; already exists");
                continue;
            }

            try {
                Main.processInput("-visualizer", visualizer.getKey(), "-size", "150", "100");
                Waveform waveform = new Waveform(false);
                if (!visualizer.supportsPlayerMode()) {
                    manualVisualizers.add(visualizer);
                    if (verbose) System.out.println("Skipped icon for " + visualizer.getName() + "; requires manual creation");
                    continue;
                }
                waveform.play(inputFile);
                waveform.exportManager.exportFullImage(outputFile, true);
                System.out.println("Exported icon for " + visualizer.getName());
                waveform.destroy();
                imagesCreated++;
            } catch (Exception e) {
                System.err.println("Error creating image for " + visualizer.getName());
                e.printStackTrace();
            }
        }
        if (imagesCreated == 0) System.out.println("No new icons were created.");
        else System.out.println("Successfully created icons for " + imagesCreated + " visualizers.");
        if (!manualVisualizers.isEmpty()) {
            String manualVisualizerNames = manualVisualizers.stream()
                    .map(Visualizer::getName)
                    .collect(Collectors.joining(", "));
            System.out.println("Icons for the following visualizers still require manual creation: " + manualVisualizerNames);
        }
    }

    private static List<Visualizer> collectVisualizers(CommandLine commandLine) {
        if (!commandLine.hasOption("visualizers")) return Arrays.asList(Visualizer.values());
        List<Visualizer> visualizers = new ArrayList<>();
        String[] visualizerNames = commandLine.getOptionValue("visualizers").split(",");
        for (String visualizerName : visualizerNames) {
            Visualizer visualizer = Visualizer.fromKey(visualizerName);
            if (visualizer == null) System.err.println("Skipped unknown visualizer: " + visualizerName);
            else visualizers.add(visualizer);
        }
        return visualizers;
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption("help", "Print this message");
        options.addRequiredOption("input", "input", true, "Audio file to create icons from");
        options.addRequiredOption("output", "output", true, "Output directory");
        options.addOption("visualizers", true, "Comma separated list of visualizers to process. If absent, will process all visualizers");
        options.addOption("clean", "Remove icons that don't correspond to a visualizer");
        options.addOption("overwrite", "Overwrite existing icons with newly created ones");
        options.addOption("verbose", "Enable verbose output");
        return options;
    }
}
