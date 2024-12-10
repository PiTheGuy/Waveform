package pitheguy.waveform.ui.drawers.spectogram;

import pitheguy.waveform.io.DrawContext;

import java.util.function.DoubleUnaryOperator;

public abstract class ScaledSpectrogramDrawer extends AbstractSpectrogramDrawer {
    public ScaledSpectrogramDrawer(DrawContext context) {
        super(context);
    }

    protected double[] resample(double[] data) {
        return resample(data, playingAudio.sampleRate(), this::rescale);
    }

    protected double[] resample(double[] data, double sampleRate, DoubleUnaryOperator rescaler) {
        double maxScaled = rescaler.applyAsDouble(sampleRate);
        double scalePerPixel = maxScaled / getImageHeight(context);
        double[] finalData = new double[getImageHeight(context)];
        double[] weightCounter = new double[getImageHeight(context)];
        for (int i = 0; i < data.length - 1; i++) {
            double frequencyStart = i * sampleRate / (double) data.length;
            double frequencyEnd = (i + 1) * sampleRate / (double) data.length;
            double scaledStart = rescaler.applyAsDouble(frequencyStart);
            double scaledEnd = rescaler.applyAsDouble(frequencyEnd);
            double magnitude = data[i];
            int startIndex = (int) (scaledStart / scalePerPixel);
            int endIndex = Math.min((int) (scaledEnd / scalePerPixel), finalData.length - 1);
            for (int j = startIndex; j <= endIndex; j++) {
                double binFraction = (j * scalePerPixel - scaledStart) / (scaledEnd - scaledStart);
                double contribution = magnitude * (1 - binFraction);
                finalData[j] += contribution;
                weightCounter[j] += 1 - binFraction;
            }
        }
        for (int i = 0; i < finalData.length; i++)
            if (weightCounter[i] > 0) finalData[i] /= weightCounter[i];

        return finalData;
    }

    public abstract double rescale(double frequency);
}
