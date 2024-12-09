package pitheguy.waveform.ui.visualizer;

import org.apache.commons.cli.*;
import pitheguy.waveform.main.*;
import pitheguy.waveform.ui.Waveform;
import pitheguy.waveform.util.Util;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class VisualizerImageCollector {

    public static void main(String[] args) throws Exception {
        Options options = createOptions();
        CommandLine commandLine = new DefaultParser().parse(options, args);
        if (commandLine.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar VisualizerImageCollector.jar", options);
            return;
        }

        Path outputPath = Path.of(commandLine.getOptionValue("output"));
        List<Visualizer> visualizers = collectVisualizers(commandLine);
        boolean overwrite = commandLine.hasOption("overwrite");
        if (commandLine.hasOption("clean")) clean(outputPath);
        if (commandLine.hasOption("input")) {
            File inputFile = new File(commandLine.getOptionValue("input"));
            createImages(inputFile, outputPath, visualizers, overwrite);
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
                String filename = Util.stripExtension(file.getName());
                if (Visualizer.fromKey(filename) == null) {
                    if (file.delete()) System.out.println("Deleted " + file.getName());
                    else System.err.println("Error deleting " + file.getName());
                }
            }
        } else System.out.println("Cleaning skipped.");
        scanner.close();
    }

    private static void createImages(File inputFile, Path outputPath, List<Visualizer> visualizers, boolean overwrite) throws Exception {
        for (Visualizer visualizer : visualizers) {
            File outputFile = outputPath.resolve(visualizer.getKey() + ".png").toFile();
            if (!overwrite && outputFile.exists()) {
                System.out.println("Skipped image for " + visualizer.getName() + "; already exists");
                continue;
            }
            Main.processInput("-visualizer", visualizer.getKey(), "-size", "150", "100");
            Waveform waveform = new Waveform(false);
            waveform.play(inputFile);
            waveform.exportManager.exportFullImage(outputFile);
            System.out.println("Exported image for " + visualizer.getName());
            waveform.destroy();
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
        options.addRequiredOption("input", "input", true, "Audio file to create images from");
        options.addRequiredOption("output", "output", true, "Output directory");
        options.addOption("visualizers", true, "Comma separated list of visualizers to process. If absent, will process all visualizers");
        options.addOption("clean", "Remove images that don't correspond to a visualizer");
        options.addOption("overwrite", "Overwrite existing images with newly created ones");
        return options;
    }
}
