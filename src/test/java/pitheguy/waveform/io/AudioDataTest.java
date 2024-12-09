package pitheguy.waveform.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AudioDataTest {
    @Test
    void testGetMonoDataSingleChannel() {
        short[] left = {10, 20, 30};
        short[] right = {10, 20, 30};
        AudioData audioData = new AudioData(left, right, 44100.0, 1.0);
        short[] monoData = audioData.getMonoData();
        assertArrayEquals(new short[]{10, 20, 30}, monoData);
    }

    @Test
    void testGetMonoDataAveragedChannels() {
        short[] left = {10, 20, 30};
        short[] right = {20, 30, 40};
        AudioData audioData = new AudioData(left, right, 44100.0, 1.0);
        short[] monoData = audioData.getMonoData();
        assertArrayEquals(new short[]{15, 25, 35}, monoData);
    }

    @Test
    void testAverageChannelsSingleChannel() {
        short[] channel1 = {5, 10, 15};
        short[] result = AudioData.averageChannels(channel1);
        assertArrayEquals(new short[]{5, 10, 15}, result);
    }

    @Test
    void testAverageChannelsMultipleChannels() {
        short[] channel1 = {10, 20, 30};
        short[] channel2 = {20, 30, 40};
        short[] channel3 = {30, 40, 50};
        short[] result = AudioData.averageChannels(channel1, channel2, channel3);
        assertArrayEquals(new short[]{20, 30, 40}, result);
    }

    @Test
    void testAverageChannelsInterweavedSingleChannel() {
        short[] sampleData = {10, 20, 30};
        short[] result = AudioData.averageChannelsInterweaved(sampleData, 1);
        assertArrayEquals(new short[]{10, 20, 30}, result);
    }

    @Test
    void testAverageChannelsInterweavedStereo() {
        short[] sampleData = {10, 20, 30, 40, 50, 60}; // Interleaved stereo data
        short[] result = AudioData.averageChannelsInterweaved(sampleData, 2);
        assertArrayEquals(new short[]{15, 35, 55}, result);
    }

    @Test
    void testAverageChannelsInterweavedTripleChannel() {
        short[] sampleData = {10, 20, 30, 40, 50, 60, 70, 80, 90}; // Interleaved data for 3 channels
        short[] result = AudioData.averageChannelsInterweaved(sampleData, 3);
        assertArrayEquals(new short[]{20, 50, 80}, result);
    }

    @Test
    void testAverageChannelsInterweavedWithEmptyData() {
        short[] sampleData = {};
        short[] result = AudioData.averageChannelsInterweaved(sampleData, 2);
        assertArrayEquals(new short[]{}, result);
    }
}