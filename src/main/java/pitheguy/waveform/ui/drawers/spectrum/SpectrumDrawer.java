package pitheguy.waveform.ui.drawers.spectrum;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;
import pitheguy.waveform.ui.drawers.BarGraphDrawer;
import pitheguy.waveform.util.FftAnalyser;
import pitheguy.waveform.util.Util;

import java.awt.image.BufferedImage;

public class SpectrumDrawer extends BarGraphDrawer {

    public SpectrumDrawer(DrawContext context) {
        super(context, true);
    }

    @Override
    protected BufferedImage drawAudio(double sec, double length) {
        super.drawAudio(sec, length);
        short[] data = AudioData.averageChannels(left, right);
        double[] magnitudes = FftAnalyser.performFFT(Util.normalize(data));
        return drawArray(Util.normalize(magnitudes), createBlankImage());
    }

    @Override
    public boolean isSeekingAllowed() {
        return Config.playerMode;
    }
}
