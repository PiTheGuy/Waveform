package pitheguy.waveform.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pitheguy.waveform.config.Config;
import pitheguy.waveform.main.WaveColor;
import pitheguy.waveform.ui.Waveform;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {
    private static final Logger LOGGER = LogManager.getLogger(Util.class);

    public static boolean areUnique(Object... elements) {
        Set<Object> uniqueElements = new HashSet<>(Arrays.asList(elements));
        return uniqueElements.size() == elements.length;
    }

    public static void logTimeTaken(Runnable task) {
        long startTime = System.currentTimeMillis();
        task.run();
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Elapsed time: " + elapsedTime + " ms");
    }

    public static void logTimeTakenMicros(Runnable task) {
        long startTime = System.nanoTime();
        task.run();
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        System.out.println("Elapsed time: " + (elapsedTime / 1000) + " μs");
    }

    public static void printError(String error) {
        System.err.println(error);
        System.exit(1);
    }
    public static String normalizeUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null || scheme.equalsIgnoreCase("http")) scheme = "https";
            String host = uri.getHost();
            if (host == null) return null;
            if (!host.contains(".")) return null;
            if (host.indexOf('.') == host.lastIndexOf('.')) host = "www." + host;
            URI normalizedUri = new URI(scheme, uri.getUserInfo(), host, uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
            return normalizedUri.toString();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static void runInBackground(ThrowingRunnable task) {
        runInBackground(task, false);
    }

    public static void runInBackground(ThrowingRunnable task, boolean waitUntilFinished) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                task.run();
                return null;
            }
        };
        worker.execute();
        if (waitUntilFinished) {
            try {
                worker.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static double[] normalize(double[] data) {
        double max = Arrays.stream(data).max().orElse(1.0);
        return Arrays.stream(data).map(datum -> datum / max).toArray();
    }

    public static double[][] crossNormalize(double[] data1, double[] data2) {
        double max = Stream.of(data1, data2).flatMapToDouble(Arrays::stream).max().orElse(1.0);
        double[][] result = new double[2][];
        result[0] = Arrays.stream(data1).map(datum -> datum / max).toArray();
        result[1] = Arrays.stream(data2).map(datum -> datum / max).toArray();
        return result;
    }

    public static double[][] normalize(double[][] data) {
        double max = Arrays.stream(data).flatMapToDouble(Arrays::stream).max().orElse(1.0);
        int maxLength = Arrays.stream(data).mapToInt(datum -> datum.length).max().orElse(0);
        double[][] normalized = new double[data.length][maxLength];
        for (int i = 0; i < data.length; i++)
            for (int j = 0; j < data[i].length; j++) normalized[i][j] = data[i][j] / max;
        return normalized;
    }

    public static double[] normalize(short[] data) {
        double[] normalizedData = new double[data.length];
        for (int i = 0; i < data.length; i++) normalizedData[i] = data[i] / 32768.0;
        return normalizedData;
    }

    public static double[][] transpose(double[][] array) {
        double[][] newArray = new double[array[0].length][array.length];
        for (int i = 0; i < array.length; i++) for (int j = 0; j < array[0].length; j++) newArray[j][i] = array[i][j];
        return newArray;
    }

    public static Color blendColor(double delta, Color start, Color end) {
        delta = Math.clamp(delta, 0, 1);
        int red = (int) ((1 - delta) * start.getRed() + delta * end.getRed());
        int green = (int) ((1 - delta) * start.getGreen() + delta * end.getGreen());
        int blue = (int) ((1 - delta) * start.getBlue() + delta * end.getBlue());
        return new Color(red, green, blue);
    }

    public static Color parseColor(String color) {
        WaveColor waveColor = WaveColor.fromName(color);
        if (waveColor != null) return waveColor.getColor();
        else if (color.startsWith("#") && color.length() == 7) return Color.decode(color);
        else return null;
    }

    public static String writeColor(Color color) {
        String hexString = Integer.toHexString(color.getRGB() & 0xFFFFFF);
        return "#" + "0".repeat(6 - hexString.length()) + hexString;
    }

    public static double lerp(double delta, double min, double max) {
        return min + delta * (max - min);
    }

    public static int lerp(double delta, int min, int max) {
        return (int) (min + delta * (max - min));
    }

    public static String getEnumKeys(Class<? extends Enum<?>> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants()).map(v -> v.name().toLowerCase()).collect(Collectors.joining(", "));
    }

    public static void showErrorOnException(ThrowingRunnable task, String message, Logger logger) {
        try {
            task.run();
        } catch (Exception e) {
            logger.error(message, e);
            Waveform.getInstance().showError("Error", message + ": " + e.getMessage());
        }
    }

    public static Process runProcess(String... command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        if (Config.debug) processBuilder.redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT);
        return processBuilder.start();
    }

    public static Process runProcess(List<String> command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        if (Config.debug) processBuilder.redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT);
        return processBuilder.start();
    }

    public static void openUrl(String url, String name) {
        if (!HttpUtil.ensureInternetConnection()) return;
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException e) {
            LOGGER.error("Failed to open {} page", name, e);
            Waveform.getInstance().showError("Error", "Failed to open " + name + " page. Check logs for more information.");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
