package pitheguy.waveform.ui.util;

import java.util.*;

public class BeatDetectionHelper {
    public static double getCutoff(Collection<Double> history, boolean increasedSensitivity) {
        double average = history.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        if (increasedSensitivity) return average;
        return average + stdDev(history.stream().mapToDouble(Double::doubleValue).toArray(), average);
    }

    private static double stdDev(double[] data, double average) {
        return Math.sqrt(Arrays.stream(data).map(value -> (value - average) * (value - average)).average().orElseThrow());
    }

}
