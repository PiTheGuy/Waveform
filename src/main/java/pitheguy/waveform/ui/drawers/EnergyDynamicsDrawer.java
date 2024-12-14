package pitheguy.waveform.ui.drawers;

import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.stream.Stream;

public class EnergyDynamicsDrawer extends LineGraphDrawer {

    public EnergyDynamicsDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected BufferedImage precomputeImage() {
        double[][] frequencyData = Util.normalize(FftAnalyser.getFrequencyData(playingAudio.getMonoData(), context.getWidth()));
        int numBands = context.getWidth();
        double[][] bandEnergies = new double[context.getWidth()][numBands];
        double scale = (double) frequencyData.length / numBands;
        for (int time = 0; time < frequencyData.length; time++) {
            for (int band = 0; band < numBands; band++) {
                int start = (int) (band * scale);
                int end = (int) ((band + 1) * scale);
                bandEnergies[time][band] = Arrays.stream(frequencyData[time], start, end).map(v -> v * v).sum();
            }
        }
        double[] eMin = new double[numBands];
        double[] eMax = new double[numBands];
        for (int band = 0; band < numBands; band++) {
            int finalBand = band;
            eMin[band] = Arrays.stream(bandEnergies).mapToDouble(frame -> frame[finalBand]).min().orElse(0.0);
            eMax[band] = Arrays.stream(bandEnergies).mapToDouble(frame -> frame[finalBand]).max().orElse(0.0);
        }
        double[] scaledEMin = Arrays.stream(eMin).map(Math::log10).toArray();
        double[] scaledEMax = Arrays.stream(eMax).map(Math::log10).toArray();
        BufferedImage image = createBlankImage();
        Graphics2D g = image.createGraphics();
        Result result = normalize(scaledEMin, scaledEMax);
        drawData(g, result.min());
        drawData(g, result.max());
        return image;
    }

    private static Result normalize(double[] eMin, double[] eMax) {
        double min = Stream.of(eMin, eMax).flatMapToDouble(Arrays::stream).min().orElse(0.0);
        double max = Stream.of(eMin, eMax).flatMapToDouble(Arrays::stream).max().orElse(0.0);
        double[] normMin = Arrays.stream(eMin).map(datum -> (datum - min) / (max - min)).toArray();
        double[] normMax = Arrays.stream(eMax).map(datum -> (datum - min) / (max - min)).toArray();
        return new Result(normMin, normMax);
    }

    public record Result(double[] min, double[] max) {
    }
}
