package pitheguy.waveform.ui.drawers.waveform;

public class DifferenceWaveformDrawer extends ResampledWaveformDrawer {

    public DifferenceWaveformDrawer(boolean forceFullAudio) {
        super(forceFullAudio);
    }

    @Override
    protected void updateMaxValue() {
        maxValue = leftResampled[0];
        for (short value : leftResampled) maxValue = (short) Math.max(maxValue, value);
        for (short value : rightResampled) maxValue = (short) Math.max(maxValue, value);
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
