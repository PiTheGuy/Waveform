package pitheguy.waveform.ui.drawers.waveform;

import org.jcodec.audio.Audio;
import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.io.DrawContext;

public abstract class ResampledWaveformDrawer extends WaveformDrawer {
    protected AudioData resampledData;

    public ResampledWaveformDrawer(DrawContext context) {
        super(context);
    }

    @Override
    protected void updateAudioData(double sec, double length) {
        if (resampledData == null) return;
        AudioData frameData = resampledData.clip(sec, length);
        left = frameData.left();
        right = frameData.right();
    }

    @Override
    public void setPlayingAudio(AudioData playingAudio) {
        this.playingAudio = playingAudio;
        short[] left = resample(playingAudio.left());
        short[] right = resample(playingAudio.right());
        resampledData = new AudioData(left, right, playingAudio.sampleRate(), playingAudio.duration());
        updateMaxValue();
    }

    public abstract short[] resample(short[] data);
}
