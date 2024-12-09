package pitheguy.waveform.util;

import org.apache.commons.math3.complex.Complex;
import org.jtransforms.fft.DoubleFFT_1D;
import pitheguy.waveform.ui.Waveform;

import java.util.Arrays;
import java.util.stream.IntStream;

public class FftAnalyser {
    public static double[] performFFT(double[] audioData) {
        int n = audioData.length;
        DoubleFFT_1D fft = new DoubleFFT_1D(n);
        return doFFT(fft, n, audioData);
    }

    public static double[][] batchFFT(double[][] audioData) {
        double[][] result = new double[audioData.length][];
        int n = audioData[0].length;
        DoubleFFT_1D fft = new DoubleFFT_1D(n);
        IntStream.range(0, audioData.length).parallel().forEach(x -> result[x] = doFFT(fft, n, audioData[x]));
        return result;
    }

    private static double[] doFFT(DoubleFFT_1D fft, int n, double[] audioData) {
        double[] fftData = new double[n * 2];
        System.arraycopy(audioData, 0, fftData, 0, n);
        fft.realForwardFull(fftData);
        double[] magnitudes = new double[n / 2];
        for (int i = 0; i < magnitudes.length; i++) {
            double real = fftData[2 * i];
            double imag = fftData[2 * i + 1];
            magnitudes[i] = Math.sqrt(real * real + imag * imag);
        }
        return magnitudes;
    }

    public static Complex[][] batchFullFFT(double[][] magnitudes) {
        Complex[][] result = new Complex[magnitudes.length][];
        int n = magnitudes[0].length;
        DoubleFFT_1D fft = new DoubleFFT_1D(n);
        IntStream.range(0, magnitudes.length).parallel().forEach(x -> result[x] = doFullFFT(fft, n, magnitudes[x]));
        return result;
    }

    private static Complex[] doFullFFT(DoubleFFT_1D fft, int n, double[] audioData) {
        double[] fftData = new double[n * 2];
        System.arraycopy(audioData, 0, fftData, 0, n);
        fft.realForwardFull(fftData);
        Complex[] results = new Complex[n / 2];
        for (int i = 0; i < results.length; i++) {
            results[i] = new Complex(fftData[2 * i], fftData[2 * i + 1]);
        }
        return results;
    }

    public static double[][] batchInverseFFT(double[][] magnitudes) {
        double[][] result = new double[magnitudes.length][];
        int n = magnitudes[0].length;
        DoubleFFT_1D fft = new DoubleFFT_1D(n);
        IntStream.range(0, magnitudes.length).parallel().forEach(x -> result[x] = doInverseFFT(
                Arrays.stream(magnitudes[x])
                        .mapToObj(v -> new Complex(v, 0))
                        .toArray(Complex[]::new), fft)
        );
        return result;
    }

    public static double[][] batchInverseFFT(Complex[][] data) {
        int n = data[0].length;
        DoubleFFT_1D fft = new DoubleFFT_1D(n);
        return Arrays.stream(data).parallel().map(datum -> doInverseFFT(datum, fft)).toArray(double[][]::new);
    }

    private static double[] doInverseFFT(Complex[] data, DoubleFFT_1D fft) {
        int n = data.length * 2;
        double[] fftData = new double[n];
        for (int i = 0; i < data.length; i++) {
            fftData[2 * i] = data[i].getReal();
            fftData[2 * i + 1] = data[i].getImaginary();
        }
        fft.realInverse(fftData, true);
        return Arrays.copyOfRange(fftData, 0, n / 2);
    }

    public static double[] resampleMagnitudesToBands(double[] magnitudes, int numBands) {
        double[] bandMagnitudes = new double[numBands];
        double magnitudesPerBand = (double) magnitudes.length / numBands;

        for (int i = 0; i < numBands; i++) {
            double sum;
            int start = (int) (i * magnitudesPerBand);
            int end = (int) Math.min(start + magnitudesPerBand, magnitudes.length);
            sum = Arrays.stream(magnitudes, start, end).sum();
            bandMagnitudes[i] = sum / (end - start);
        }

        return bandMagnitudes;
    }

    public static double[][] getFrequencyData(short[] sampleData, int numSamples) {
        double samplesPerPixel = (double) sampleData.length / numSamples;
        double[][] audioData = new double[numSamples][];
        int minFFTSize = Waveform.HEIGHT * 2;
        int arraySize = (int) Math.max(samplesPerPixel, minFFTSize);
        for (int x = 0; x < numSamples; x++) {
            sliceAndNormalize(sampleData, samplesPerPixel, audioData, arraySize, x);
        }
        return batchFFT(audioData);
    }

    public static Complex[][] getComplexFrequencyData(short[] sampleData, int width) {
        double samplesPerPixel = (double) sampleData.length / width;
        double[][] audioData = new double[width][];
        int minFFTSize = Waveform.HEIGHT * 2;
        int arraySize = (int) Math.max(samplesPerPixel, minFFTSize);
        for (int x = 0; x < width; x++) {
            sliceAndNormalize(sampleData, samplesPerPixel, audioData, arraySize, x);
        }
        return batchFullFFT(audioData);
    }

    private static void sliceAndNormalize(short[] sampleData, double samplesPerPixel, double[][] audioData, int arraySize, int x) {
        final int start = (int) (x * samplesPerPixel);
        int sampleLength = (int) samplesPerPixel;
        short[] stripData = new short[sampleLength];
        System.arraycopy(sampleData, start, stripData, 0, Math.min(sampleLength, sampleData.length - start));
        audioData[x] = Util.normalize(Arrays.copyOf(stripData, arraySize));
    }
}
