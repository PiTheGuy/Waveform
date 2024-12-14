package pitheguy.waveform.ui.drawers.waveform;

import pitheguy.waveform.io.DrawContext;

public class DifferenceWaveformDrawer extends ResampledWaveformDrawer {

    public DifferenceWaveformDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected void updateMaxValue() {
        maxValue = left[0];
        for (short value : left) maxValue = (short) Math.max(maxValue, value);
        for (short value : right) maxValue = (short) Math.max(maxValue, value);
    }

    public short[] resample(short[] data) {
        short[] diff = new short[data.length];
        for (int i = 0; i < data.length; i++) {
            if (i == 0) diff[i] = 0;
            else diff[i] = (short) Math.abs(data[i] - data[i - 1]);
        }
        return diff;
    }
}
