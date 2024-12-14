package pitheguy.waveform.ui.drawers.waveform;

import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;

public abstract class ResampledWaveformDrawer extends WaveformDrawer {
    protected short[] leftResampled;
    protected short[] rightResampled;

    public ResampledWaveformDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected void updateAudioData(double sec, double length) {
        if (leftResampled == null || rightResampled == null) return;
        int framesThisSample = (int) Math.min(length * playingAudio.sampleRate(), (playingAudio.duration() - sec) * playingAudio.sampleRate());
        System.arraycopy(leftResampled, (int) (sec * playingAudio.sampleRate()), left, 0, framesThisSample);
        System.arraycopy(rightResampled, (int) (sec * playingAudio.sampleRate()), right, 0, framesThisSample);
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        this.playingAudio = playingAudio;
        leftResampled = resample(playingAudio.left());
        rightResampled = resample(playingAudio.right());
        updateMaxValue();
    }

    public abstract short[] resample(short[] data);
}
