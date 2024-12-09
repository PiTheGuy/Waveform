package pitheguy.waveform.util.rolling;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

public class RollingAverageTracker<T> {
    private final RollingList<T> values;
    private final Function<Stream<T>, T> averageFunction;

    public RollingAverageTracker(int size, Function<Stream<T>, T> averageFunction) {
        values = new RollingList<>(size);
        this.averageFunction = averageFunction;
    }

    public void add(T value) {
        values.add(value);
    }

    public T getAverage() {
        if (values.isEmpty()) throw new IllegalStateException("Can not get average on empty list");
        return averageFunction.apply(values.stream());
    }

    public void clear() {
        values.clear();
    }

    public static class DoubleTracker extends RollingAverageTracker<Double> {
        public DoubleTracker(int size) {
            super(size, stream -> stream.mapToDouble(Double::doubleValue).average().orElseThrow());
        }
    }

    public static class DoubleArrayTracker extends RollingAverageTracker<double[]> {
        public DoubleArrayTracker(int size) {
            super(size, stream -> {
                double[][] values = stream.toArray(double[][]::new);
                double[] result = new double[values[0].length];
                for (double[] array : values) for (int i = 0; i < array.length; i++) result[i] += array[i];
                Arrays.setAll(result, i -> result[i] / values.length);
                return result;
            });
        }
    }
}
