package pitheguy.waveform.io.parsers;

import pitheguy.waveform.io.AudioData;
import pitheguy.waveform.config.Config;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class AudioSteamParser implements AudioParser {
    public abstract AudioInputStream getStream(File audioFile) throws IOException, UnsupportedAudioFileException;

    @Override
    public AudioData parse(File audioFile) {
        try {
            return parseStream(getStream(audioFile));
        } catch (Exception e) {
            return null;
        }
    }

    public AudioData parseStream(AudioInputStream stream) throws Exception {
        AudioFormat format = stream.getFormat();
        int numSamples = (int) stream.getFrameLength();
        int numChannels = format.getChannels();
        int bytesPerSample = format.getSampleSizeInBits() / 8;
        double duration = (double) numSamples / format.getFrameRate();
        short[] sampleData = new short[numSamples * numChannels];
        byte[] audioData = new byte[numSamples * bytesPerSample * numChannels];
        stream.read(audioData);
        ByteBuffer byteBuffer = ByteBuffer.wrap(audioData).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < sampleData.length; i++) {
            sampleData[i] = byteBuffer.getShort();
        }
        if (Config.mono) {
            short[] data = AudioData.averageChannelsInterweaved(sampleData, numChannels);
            return new AudioData(data, data, format.getFrameRate(), duration);
        }
        if (numChannels == 1) return new AudioData(sampleData, sampleData, format.getFrameRate(), duration);
        else if (numChannels == 2) {
            short[] left = new short[numSamples];
            short[] right = new short[numSamples];
            for (int i = 0; i < numSamples; i++) {
                left[i] = sampleData[i * 2];
                right[i] = sampleData[i * 2 + 1];
            }
            return new AudioData(left, right, format.getFrameRate(), duration);
        }
        else {
            System.out.println("Warning: Multi-channel visualization not supported; too many channels");
            short[] data = AudioData.averageChannelsInterweaved(sampleData, numChannels);
            return new AudioData(data, data, format.getFrameRate(), duration);
        }
    }

}