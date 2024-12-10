package pitheguy.waveform.ui.drawers.spectogram;

import org.apache.commons.math3.complex.Complex;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.drawers.HeatmapDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class PhaseSpectrogramDrawer extends HeatmapDrawer {
    public PhaseSpectrogramDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected BufferedImage precomputeImage() {
        short[] monoData = playingAudio.getMonoData();
        Complex[][] fftData = FftAnalyser.getComplexFrequencyData(monoData, getImageWidth());
        double[][] phases = new double[getImageWidth()][];
        for (int i = 0; i < fftData.length; i++)
            for (int j = 0; j < fftData[i].length; j++)
                phases[i][j] = getPhase(fftData[i][j].getReal(), fftData[i][j].getImaginary());
        double[][] resampledPhases = new double[getImageWidth()][];
        Arrays.setAll(resampledPhases, i -> FftAnalyser.resampleMagnitudesToBands(phases[i], getImageHeight(context)));
        resampledPhases = Util.normalize(resampledPhases);
        return drawData(context, resampledPhases);
    }

    public static double getPhase(double real, double imag) {
        double phase = Math.atan2(imag, real) % (2 * Math.PI);
        if (phase < 0) phase += 2 * Math.PI;
        return phase;
    }
}
