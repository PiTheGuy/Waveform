package pitheguy.waveform.io;

import pitheguy.waveform.config.Config;
import pitheguy.waveform.io.microphone.MicrophoneCapture;

import java.util.Arrays;

public record AudioData(short[] left, short[] right, double sampleRate, double duration) {

    public static AudioData fromMicrophone(short[] data) {
        return new AudioData(data, data, MicrophoneCapture.SAMPLE_RATE, Config.getFrameLength());
    }

    public short[] getMonoData() {
        return averageChannels(left, right);
    }

    public AudioData clip(double sec, double length) {
        if (length < 0) throw new IllegalArgumentException("length must be > 0");
        if (sec < 0 || sec > duration) throw new IndexOutOfBoundsException("Clip out of audio bounds");
        double effectiveLength = Math.min(length, duration - sec);
        int from = (int) (sec * sampleRate);
        int to = (int) ((sec + effectiveLength) * sampleRate);
        short[] newLeft = Arrays.copyOfRange(left, from, to);
        short[] newRight = Arrays.copyOfRange(right, from, to);
        return new AudioData(newLeft, newRight, sampleRate, effectiveLength);
    }

    public static short[] averageChannelsInterweaved(short[] sampleData, int channels) {
        if (channels == 1) return sampleData;
        short[] newSampleData = new short[sampleData.length / channels];
        for (int i = 0; i < newSampleData.length; i++) {
            int start = i * channels;
            int total = 0;
            for (int j = start; j < start + channels; j++) total += sampleData[j];
            newSampleData[i] = (short) (total / channels);
        }
        return newSampleData;
    }

    public static short[] averageChannels(short[]... sampleData) {
        int numChannels = sampleData.length;
        if (numChannels == 1) return sampleData[0];
        short[] newSampleData = new short[sampleData[0].length];
        for (int i = 0; i < newSampleData.length; i++) {
            int total = 0;
            for (short[] sampleDatum : sampleData) {
                total += sampleDatum[i];
            }
            newSampleData[i] = (short) (total / numChannels);
        }
        return newSampleData;
    }
}
